package org.hung.kstream.stockprocessorapi.kstream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.hung.kstream.stockprocessorapi.domain.Quote;
import org.hung.kstream.stockprocessorapi.domain.QuoteKey;
import org.springframework.kafka.support.serializer.JsonSerde;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuoteFeedsConsolidateProcessorSupplier implements ProcessorSupplier<QuoteKey,GenericRecord,QuoteKey,Quote> {

    @Override
    public Processor<QuoteKey, GenericRecord, QuoteKey, Quote> get() {
        return new QuoteFeedsConsolidateProcessor();
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("quote"), 
                new JsonSerde<>(QuoteKey.class),
                new JsonSerde<>(Quote.class))
        );
    }
    
    static class QuoteFeedsConsolidateProcessor implements Processor<QuoteKey,GenericRecord,QuoteKey,Quote> {

        private ProcessorContext<QuoteKey,Quote> context;
        private KeyValueStore<QuoteKey, Quote> quoteStore;

        @Override
        public void init(final ProcessorContext<QuoteKey, Quote> context) {
            this.context = context;
            this.quoteStore = context.getStateStore("quote");
        }

        @Override
        public void process(final Record<QuoteKey, GenericRecord> record) {
            Header srcTopicHeader = record.headers().lastHeader("src_topic");
            GenericRecord feedRec = record.value();

            String srcTopic = new String(srcTopicHeader.value(),StandardCharsets.UTF_8);

            String market = ((Utf8)feedRec.get("market")).toString();
            String ticker = ((Utf8)feedRec.get("ticker")).toString();
            LocalDate tradeDate = LocalDate.ofEpochDay((Integer)feedRec.get("trade_date"));
            
            QuoteKey key = new QuoteKey();
            key.setMarket(market);
            key.setTicker(ticker);
            key.setTradeDate(tradeDate);


            Quote quote = this.quoteStore.get(key);
            log.info("get quote {} from store by key {}", quote, key);
            if (Objects.isNull(quote)) {
                quote = new Quote();
                quote.setMarket(market);
                quote.setTicker(ticker);
                quote.setTradeDate(tradeDate);
            }

            if ("postgres_stock_price_feed".equalsIgnoreCase(srcTopic)) {
                ByteBuffer bytes = (ByteBuffer)feedRec.get("price");
                BigDecimal price = new BigDecimal(new BigInteger(bytes.array()),2);

                if (Objects.isNull(quote.getOpen())) {
                    quote.setOpen(price);
                    quote.setLow(price);
                    quote.setHigh(price);
                    quote.setSpot(price);
                    quote.setChange(BigDecimal.ZERO);
                } else {
                    quote.setLow(quote.getLow().min(price));
                    quote.setHigh(quote.getHigh().max(price));
                    quote.setSpot(price);
                    quote.setChange(price.subtract(quote.getOpen()).divide(quote.getOpen(),2,RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
                }
                quote.setVer(quote.getVer()+1);
                quote.setLastUpdDate(Instant.now());
                quoteStore.put(key, quote);
        
                context.forward(new Record<>(key,quote,Instant.now().toEpochMilli()));
                return;
            }

            if ("postgres_stock_volume_feed".equalsIgnoreCase(srcTopic)) {
                ByteBuffer bytes = (ByteBuffer)feedRec.get("volume");
                BigInteger volume = new BigInteger(bytes.array());

                quote.setVolume(volume);
                quote.setVer(quote.getVer()+1);
                quote.setLastUpdDate(Instant.now());
                quoteStore.put(key, quote);
        
                context.forward(new Record<>(key,quote,Instant.now().toEpochMilli()));
                return;
            }
            return;
        }

        @Override
        public void close() {

        }
        
    }
}
