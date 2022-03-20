package com.example.kafkastreamsinaction.streams;


import com.example.kafkastreamsinaction.joiner.PurchaseJoiner;
import com.example.kafkastreamsinaction.model.CorrelatedPurchase;
import com.example.kafkastreamsinaction.model.Purchase;
import com.example.kafkastreamsinaction.serde.PurchaseSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.BiConsumer;

@Slf4j
@Configuration
public class ZMartStreamsJoinsApplicationConfiguration {

    private static final Serde<Purchase> purchaseSerde = new PurchaseSerde();

    @Bean
    public BiConsumer<KStream<String, Purchase>, KStream<String, Purchase>> zMartStreamsJoinsApplication() {
        return (coffeeStream, electionStream) -> {
            JoinWindows twentyMinuteWindow = JoinWindows.of(Duration.ofMinutes(20));
            KStream<String, CorrelatedPurchase> joinStream = coffeeStream.join(electionStream,
                    new PurchaseJoiner(),
                    twentyMinuteWindow,
                    StreamJoined.with(Serdes.String(), purchaseSerde, purchaseSerde));

            joinStream.print(Printed.<String, CorrelatedPurchase>toSysOut()
                    .withLabel("[JoinedStream]"));
        };
    }

}
