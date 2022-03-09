package com.example.kafkastreamsinaction.chapter03;

import com.example.kafkastreamsinaction.model.Purchase;
import com.example.kafkastreamsinaction.model.PurchasePattern;
import com.example.kafkastreamsinaction.model.RewardAccumulator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class ZMartStreamsApplicationConfiguration {

    private static final Serde<String> stringSerde = Serdes.String();
    private static final Serde<Purchase> purchaseSerde = new JsonSerde<>(Purchase.class);
    private static final Serde<PurchasePattern> purchasePatternSerde = new JsonSerde<>(PurchasePattern.class);
    private static final Serde<RewardAccumulator> rewardAccumulatorSerde = new JsonSerde<>(RewardAccumulator.class);

    @Bean
    public Consumer<KStream<String, Purchase>> zMartStreamApplication() {
        return input -> {
            final KStream<String, Purchase> purchaseKStream = input
                    .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

            purchaseKStream
                    .mapValues(purchase -> PurchasePattern.builder(purchase).build())
                    .peek((key, value) -> log.info("[pattern] key {}, value: {}", key, value))
                    .to("patterns", Produced.with(stringSerde, purchasePatternSerde));

            purchaseKStream
                    .mapValues(purchase -> RewardAccumulator.builder(purchase).build())
                    .peek((key, value) -> log.info("[rewards] key {}, value: {}", key, value))
                    .to("rewards", Produced.with(stringSerde, rewardAccumulatorSerde));

            purchaseKStream
                    .peek((key, value) -> log.info("[purchases] key {}, value: {}", key, value))
                    .to("purchases", Produced.with(stringSerde, purchaseSerde));
        };
    }

}
