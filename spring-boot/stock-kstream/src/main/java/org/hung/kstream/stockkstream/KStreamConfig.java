package org.hung.kstream.stockkstream;

import java.time.LocalDate;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hung.kstream.stockkstream.domain.PriceTable;
import org.hung.kstream.stockkstream.domain.QuoteKey;
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
    public KTable<QuoteKey,PriceTable> dummy(StreamsBuilder builder) {

        Serde<GenericRecord> genericAvroKeySerde = new GenericAvroSerde();
        genericAvroKeySerde.configure(Map.of("schema.registry.url","http://schema-reg.kafka.svc.cluster.local:8081"), true);

        Serde<GenericRecord> genericAvroValueSerde = new GenericAvroSerde();
        genericAvroValueSerde.configure(Map.of("schema.registry.url","http://schema-reg.kafka.svc.cluster.local:8081"), false);

        KTable<QuoteKey,PriceTable> priceTable = builder.stream("postgres_stock_price_feed", Consumed.with(Serdes.String(), genericAvroValueSerde))
            .peek((k,v) -> {log.info("{0}",v);})
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
                    return aggValue;
                },
                Materialized/*.<QuoteKey, PriceTable, KeyValueStore<Bytes, byte[]>>as("aggregated-stream-store")*/
                    .with(new JsonSerde<QuoteKey>(QuoteKey.class),new JsonSerde<PriceTable>(PriceTable.class))
            );

        return priceTable;
    }
}
