package com.example.kafkastreamsinaction.chapter03;

import com.example.kafkastreamsinaction.model.Purchase;
import com.example.kafkastreamsinaction.model.PurchasePattern;
import com.example.kafkastreamsinaction.model.RewardAccumulator;
import com.example.kafkastreamsinaction.serde.PurchasePatternSerde;
import com.example.kafkastreamsinaction.serde.PurchaseSerde;
import com.example.kafkastreamsinaction.serde.RewardAccumulatorSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class ZMartStreamsApplicationConfiguration {

    private static final Serde<Purchase> purchaseSerde = new PurchaseSerde();
    private static final Serde<PurchasePattern> purchasePatternSerde = new PurchasePatternSerde();
    private static final Serde<RewardAccumulator> rewardAccumulatorSerde = new RewardAccumulatorSerde();

    @Bean
    public Consumer<KStream<String, Purchase>> zMartStreamApplication() {
        return input -> {
            final KStream<String, Purchase> purchaseKStream = input
                    .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

            purchaseKStream
                    .mapValues(purchase -> PurchasePattern.builder(purchase).build())
                    .peek((key, value) -> log.info("[pattern] key {}, value: {}", key, value))
                    .to("patterns", Produced.with(Serdes.String(), purchasePatternSerde));

            purchaseKStream
                    .mapValues(purchase -> RewardAccumulator.builder(purchase).build())
                    .peek((key, value) -> log.info("[rewards] key {}, value: {}", key, value))
                    .to("rewards", Produced.with(Serdes.String(), rewardAccumulatorSerde));

            purchaseKStream
                    .peek((key, value) -> log.info("[purchases] key {}, value: {}", key, value))
                    .filter((key, value) -> value.getPrice() > 5.00)
                    .selectKey((key, value) -> value.getPurchaseDate().getTime())
                    .to("purchases", Produced.with(Serdes.Long(), purchaseSerde));

            purchaseKStream
                    .filter((key, value) -> value.getEmployeeId().equals("source code has 000000"))
                    .foreach((key, value) -> {
                        log.info("[Security] Saving Record to Security Data Store. key {} value {}", key, value);
                    });

            /**
             * split(), branch(Predicate) 를 통해 브랜치 프로세서를 만들 수 있다. Predicate의 갯수 만큼 새로운 KStream이 생성된다.
             * defaultBranch()를 사용하면 Predicate에 일치하지 않은 Record들을 모아 Branch를 만들수 있고, noDefaultBranch()를 사용하면 일치하지 않은 레코드들은 처리하지 않는다.
             * Branched를 통해 새로 생성된 KStream을 바로 사용하거나, 또는 이름을 지정해서 Entry를 접근할 수 도 있다.
             * 자세한 내용은 BranchedKStream을 참고하자.
             */

//            Entry에 접근하기 위해서는 BranchStream에 규약을 따라야하며 split(Named.as("prefix))를 지정하여 Map의 Key를 생성할 수 있다.
//            Map<String, KStream<String, Purchase>> branchStreams = purchaseKStream.split(Named.as("department"))
//                    .branch((key, value) -> value.getDepartment().equals("coffee"), Branched.as("coffee"))
//                    .branch((key, value) -> value.getDepartment().equals("electronics"), Branched.as("electronics"))
//                    .noDefaultBranch();


            purchaseKStream.split()
                    .branch((key, value) -> value.getDepartment().equals("coffee"), Branched.withConsumer(kStream -> kStream.to("coffee")))
                    .branch((key, value) -> value.getDepartment().equals("electronics"), Branched.withConsumer(kStream -> kStream.to("electronics")))
                    .noDefaultBranch();
        };
    }

}
