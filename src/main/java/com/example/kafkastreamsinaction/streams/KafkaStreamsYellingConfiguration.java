package com.example.kafkastreamsinaction.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class KafkaStreamsYellingConfiguration {

    /**
     * spring-cloud-stream-binder-kafka에서 지원하는 Programming Model중 Functional Style 기반으로 KStream Application 등록
     * https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.2.1/reference/html/spring-cloud-stream-binder-kafka.html#_functional_style
     * @return
     */
    @Bean
    public Consumer<KStream<String, String>> kafkaStreamsYellingApp() {
        return input -> {
            input.mapValues(value -> value.toUpperCase())
                    .peek((key, value) -> System.out.println("Key: " + key + " Value: " + value))
                    .to("output-topic", Produced.with(Serdes.String(), Serdes.String()));
        };
    }

}
