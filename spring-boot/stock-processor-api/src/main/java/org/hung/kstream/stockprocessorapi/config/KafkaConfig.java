package org.hung.kstream.stockprocessorapi.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.Topology;
import org.hung.kstream.stockprocessorapi.domain.Quote;
import org.hung.kstream.stockprocessorapi.domain.QuoteKey;
import org.hung.kstream.stockprocessorapi.kstream.QuoteFeedsConsolidateProcessorSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.support.serializer.JsonSerde;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {
    
    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamBuilderFactoryConfigurer() {
        return new StreamsBuilderFactoryBeanConfigurer() {
            @Override
            public void configure(StreamsBuilderFactoryBean factoryBean) {
                factoryBean.setInfrastructureCustomizer(new MyKafkaStreamsInfrastructureCustomizer());//serdeConfig));
            }  
        };
    }

    @RequiredArgsConstructor
    class MyKafkaStreamsInfrastructureCustomizer implements KafkaStreamsInfrastructureCustomizer {


        @Override
        public void configureTopology(Topology topology) {

            Serde<QuoteKey> quoteKeySerde = new JsonSerde<>(QuoteKey.class);
            Serde<Quote> quoteSerde = new JsonSerde<>(Quote.class);

            topology.addSource("quote-feeds", "postgres_stock_price_feed","postgres_stock_volume_feed")
                .addProcessor("quote-consolidate", new QuoteFeedsConsolidateProcessorSupplier(), "quote-feeds")
                .addSink("quote-update", "quote", quoteKeySerde.serializer(), quoteSerde.serializer(), "quote-consolidate");
            /*
            topology.addSource("stock-price-feed", "postgres_stock_price_feed")
                .addProcessor("consolidate-price-feed", () -> new PriceFeedsConsolidateProcessor(), "stock-price-feed")
                .addSource("stock-volume-feed", "postgres_stock_volume_feed")
                .addProcessor("consolidate-volume-feed", () -> new VolumeFeedsConsolidateProcessor(), "stock-volume-feed")
                .addSink("quote-update", "quote",
                    Serdes.String().serializer(), quoteSerde.serializer(), 
                    "consolidate-price-feed","consolidate-volume-feed")
                .addStateStore(
                    Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("quote"), 
                        Serdes.String(), quoteSerde),
                    "consolidate-price-feed","consolidate-volume-feed");
            */
        }        
    }

}
