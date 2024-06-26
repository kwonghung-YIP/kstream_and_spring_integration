package org.hung.kstream.stockprocessorapi.kstream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hung.kstream.stockprocessorapi.domain.Quote;
import org.hung.kstream.stockprocessorapi.domain.QuoteKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriceFeedsConsolidateProcessor implements Processor<String,GenericRecord,QuoteKey,Quote>{

    private ProcessorContext<QuoteKey,Quote> context;
    private KeyValueStore<QuoteKey, Quote> store;

    @Override
    public void init(ProcessorContext<QuoteKey, Quote> context) {
        this.context = context;
        this.store = context.getStateStore("quote");
    }

    @Override
    public void process(Record<String, GenericRecord> record) {
        GenericRecord feedRec = record.value();

        String market = ((Utf8)feedRec.get("market")).toString();
        String ticker = ((Utf8)feedRec.get("ticker")).toString();
        LocalDate tradeDate = LocalDate.ofEpochDay((Integer)feedRec.get("trade_date"));
        
        ByteBuffer bytes = (ByteBuffer)feedRec.get("price");
        BigDecimal price = new BigDecimal(new BigInteger(bytes.array()),2);

        log.info("feed received ticker:{}, price:{}", ticker, price);

        QuoteKey key = new QuoteKey();
        key.setMarket(market);
        key.setTicker(ticker);
        key.setTradeDate(tradeDate);

        Quote quote = this.store.get(key);
        if (Objects.isNull(quote)) {
            quote = new Quote();
            quote.setMarket(market);
            quote.setTicker(ticker);
            quote.setTradeDate(tradeDate);
        }

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
        store.put(key, quote);

        context.forward(new Record<>(key,quote,Instant.now().toEpochMilli()));
    }

    @Override
    public void close() {

    }
}
