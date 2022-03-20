package com.example.kafkastreamsinaction.streams;

import com.example.kafkastreamsinaction.model.Purchase;
import com.example.kafkastreamsinaction.model.PurchasePattern;
import com.example.kafkastreamsinaction.model.RewardAccumulator;
import com.example.kafkastreamsinaction.serde.PurchasePatternSerde;
import com.example.kafkastreamsinaction.serde.PurchaseSerde;
import com.example.kafkastreamsinaction.serde.RewardAccumulatorSerde;
import com.example.kafkastreamsinaction.transformer.PurchaseRewardTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class ZMartStreamsApplicationConfiguration {

    private static final Serde<Purchase> purchaseSerde = new PurchaseSerde();
    private static final Serde<PurchasePattern> purchasePatternSerde = new PurchasePatternSerde();
    private static final Serde<RewardAccumulator> rewardAccumulatorSerde = new RewardAccumulatorSerde();

    private static final String REWARD_STATE_STORE = "reward-state-store";

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
                    /**
                     * repartition() 기능을 활용하여 파티션을 재조정한다.
                     * 내부에서 자동으로 해당 스트림만 사용하는 Repartion Topic을 생성해준다. (기존 Topic의 파티션 개수를 따라감)
                     * 토픽 명은 {applicationId}-<name>-repartition 규칙을 따르며 Repartitioned.as(name)을 사용하여 변경할 수 있다.
                     * repartition은 key가 있는 스트림(Record)에 대해서만 동작한다. KStreamImpl.createRepartitionedSource 에서 NullKeyFilter를 적용했기 떄문에 동작하지 않는다.
                     */
//                    .map(((key, value) -> new KeyValue<>(value.getCustomerId(), value)))
//                    .repartition(Repartitioned.with(Serdes.String(), purchaseSerde)
//                            .withStreamPartitioner(((topic, key, value, numPartitions) -> value.getCustomerId().hashCode() % numPartitions)))
                    /**
                     * 예제코드 작성을 위해 through를 사용하여 Repartition 진행. repartition() 메서드는 Key가 존재하는 스트림(Record)에 대해서만 정상 동작한다.
                     */
                    .through("customer_transactions", Produced.with(Serdes.String(), purchaseSerde, ((topic, key, value, numPartitions) -> value.getCustomerId().hashCode() % numPartitions)))
                    .transformValues(() -> new PurchaseRewardTransformer(REWARD_STATE_STORE), REWARD_STATE_STORE)
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


            purchaseKStream.selectKey((key, value) -> value.getCustomerId()).split()
                    .branch((key, value) -> value.getDepartment().equals("Coffee"), Branched.withConsumer(kStream -> kStream.to("coffee")))
                    .branch((key, value) -> value.getDepartment().equals("Electronics"), Branched.withConsumer(kStream -> kStream.to("electronics")))
                    .noDefaultBranch();
        };
    }

    /**
     * ValueTransformerSupplier에서 ConnectedStoreProvider를 통해서도 StateStore를 등록할 수 있다.
     * 이번 예제에서는 스프링에서 사용하는 Bean 방식의 StateStore 등록방식을 사용한다.
     * https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.2.1/reference/html/spring-cloud-stream-binder-kafka.html#_state_store
     *
     * @return
     */
    @Bean
    public StoreBuilder<KeyValueStore<String, Integer>> rewardsPointStore() {
        StoreBuilder<KeyValueStore<String, Integer>> stateStoreBuilder = Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(REWARD_STATE_STORE),
                Serdes.String(),
                Serdes.Integer()
        );

        /**
         * StateStore의 Chang Log Topic의 설정은 다음과 같이 변경할 수 있다.
         * Change Log Topic의 크기를 10G, 보존 기간을 2일로 설정
         */
//        Map<String, String> changeLogConfigs = new HashMap<>();
//        changeLogConfigs.put("retention.ms", "172800000");
//        changeLogConfigs.put("retention.bytes", "10000000000");

//        stateStoreBuilder.withLoggingEnabled(changeLogConfigs);
        return stateStoreBuilder;
    }

}
