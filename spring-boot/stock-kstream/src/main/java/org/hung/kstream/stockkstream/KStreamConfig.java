package org.hung.kstream.stockkstream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hung.kstream.stockkstream.domain.PriceTable;
import org.hung.kstream.stockkstream.domain.Quote;
import org.hung.kstream.stockkstream.domain.QuoteKey;
import org.hung.kstream.stockkstream.domain.VolumeTable;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.google.common.primitives.Bytes;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KStreamConfig {
    
    @Bean
    public KTable<QuoteKey,Quote> dummy(StreamsBuilder builder) {

        Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(Map.of("schema.registry.url","http://schema-reg.kafka.svc.cluster.local:8081"), true);

        Serde<GenericRecord> genericAvroValueSerde = new GenericAvroSerde();
        genericAvroValueSerde.configure(Map.of("schema.registry.url","http://schema-reg.kafka.svc.cluster.local:8081"), false);

        KTable<QuoteKey,PriceTable> priceTable = builder.stream("postgres_stock_price_feed", Consumed.with(Serdes.String(), genericAvroValueSerde))
            .<QuoteKey>groupBy((key,rec) -> {
                        var newkey = new QuoteKey();
                        newkey.setMarket(((Utf8)rec.get("market")).toString());
                        newkey.setTicker(((Utf8)rec.get("ticker")).toString());
                        newkey.setTradeDate(LocalDate.ofEpochDay((Integer)rec.get("trade_date")));
                        return newkey;
                    },
                    Grouped.<QuoteKey,GenericRecord>keySerde(new JsonSerde<QuoteKey>(QuoteKey.class)))
            .<PriceTable>aggregate(() -> new PriceTable(), 
                (key,newValue,aggValue) -> {
                    ByteBuffer bytes = (ByteBuffer)newValue.get("price");
                    BigDecimal price = new BigDecimal(new BigInteger(bytes.array()));
                    if (Objects.isNull(aggValue.getKey())) {
                        aggValue.setKey(key);
                        aggValue.setOpen(price);
                        aggValue.setLow(price);
                        aggValue.setHigh(price);
                        aggValue.setSpot(price);
                        aggValue.setChange(BigDecimal.ZERO);
                        aggValue.setVer(1);
                        aggValue.setLastUpdDate(LocalDateTime.now());
                    } else {
                        aggValue.setLow(aggValue.getLow().min(price));
                        aggValue.setHigh(aggValue.getHigh().max(price));
                        aggValue.setSpot(price);
                        aggValue.setChange(price.subtract(aggValue.getOpen()).divide(aggValue.getOpen()).multiply(BigDecimal.valueOf(100)));
                        aggValue.setVer(aggValue.getVer()+1);
                        aggValue.setLastUpdDate(LocalDateTime.now());
                    }
                    return aggValue;
                },
                Named.as("price-table"),
                Materialized/*.<QuoteKey, PriceTable, KeyValueStore<Bytes, byte[]>>as("aggregated-stream-store")*/
                    .with(new JsonSerde<QuoteKey>(QuoteKey.class),new JsonSerde<PriceTable>(PriceTable.class))
            );

        KTable<QuoteKey,VolumeTable> volumeTable = builder.stream("postgres_stock_volume_feed", Consumed.with(Serdes.String(), genericAvroValueSerde))
            .<QuoteKey>groupBy((key,rec) -> {
                        var newkey = new QuoteKey();
                        newkey.setMarket(((Utf8)rec.get("market")).toString());
                        newkey.setTicker(((Utf8)rec.get("ticker")).toString());
                        newkey.setTradeDate(LocalDate.ofEpochDay((Integer)rec.get("trade_date")));
                        return newkey;
                    },
                    Grouped.<QuoteKey,GenericRecord>keySerde(new JsonSerde<QuoteKey>(QuoteKey.class)))
            .<VolumeTable>aggregate(() -> new VolumeTable(), 
                (key,newValue,aggValue) -> {
                    ByteBuffer bytes = (ByteBuffer)newValue.get("volume");
                    BigInteger volume = new BigInteger(bytes.array());
                    if (Objects.isNull(aggValue.getKey())) {
                        aggValue.setKey(key);
                    }
                    aggValue.setVolume(volume);
                    aggValue.setVer(1);
                    aggValue.setLastUpdDate(LocalDateTime.now());
                    return aggValue;
                },
                Named.as("volume-table"),
                Materialized/*.<QuoteKey, PriceTable, KeyValueStore<Bytes, byte[]>>as("aggregated-stream-store")*/
                    .with(new JsonSerde<QuoteKey>(QuoteKey.class),new JsonSerde<VolumeTable>(VolumeTable.class))
            );

        KTable<QuoteKey,Quote> quoteTable = priceTable.join(volumeTable,
            (p,v) -> {
                Quote quote = new Quote();
                quote.setMarket(p.getKey().getMarket());
                quote.setTicker(p.getKey().getTicker());
                quote.setTradeDate(p.getKey().getTradeDate());
                quote.setOpen(p.getOpen());
                quote.setLow(p.getLow());
                quote.setHigh(p.getHigh());
                quote.setClose(p.getClose());
                quote.setSpot(p.getSpot());
                quote.setChange(p.getChange());
                quote.setVolume(v.getVolume());
                quote.setVer(p.getVer()+v.getVer());
                quote.setLastUpdDate(LocalDateTime.now());
                return quote;
            });

        quoteTable.toStream()
            .to("quote_feed",Produced.with(new JsonSerde<QuoteKey>(QuoteKey.class), new JsonSerde<Quote>(Quote.class)));

        return quoteTable;
    }
}
