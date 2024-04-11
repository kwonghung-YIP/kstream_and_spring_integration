package org.hung.kstream.stockprocessorapi.kstream;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class VolumeFeedsConsolidateProcessor implements Processor<String,GenericRecord,QuoteKey,Quote> {

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

        ByteBuffer bytes = (ByteBuffer)feedRec.get("volume");
        BigInteger volume = new BigInteger(bytes.array());

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

        quote.setVolume(volume);
        quote.setVer(quote.getVer()+1);
        quote.setLastUpdDate(LocalDateTime.now());

        store.put(key, quote);
        context.forward(new Record<>(key,quote,Instant.now().toEpochMilli()));
    }

    @Override
    public void close() {

    }
}
