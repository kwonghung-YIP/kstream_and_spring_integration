package org.hung.kstream.stockkstream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KStreamConfig {
    
    @Bean
    public KStream dummy(StreamsBuilder builder) {
        return builder.stream("postgres_stock_quote",Consumed.with(Serdes.String(),Serdes.ByteArray()));
    }
}
