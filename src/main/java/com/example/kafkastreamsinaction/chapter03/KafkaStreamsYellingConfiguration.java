package com.example.kafkastreamsinaction.chapter03;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class KafkaStreamsYellingConfiguration {

    @Bean
    public Consumer<KStream<String, String>> kafkaStreamsYellingApp() {
        return input -> {
            input.mapValues(value -> value.toUpperCase())
                    .peek((key, value) -> System.out.println("Key: " + key + " Value: " + value))
                    .to("output-topic", Produced.with(Serdes.String(), Serdes.String()));
        };
    }

}
