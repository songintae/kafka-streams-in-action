package com.example.kafkastreamsinaction.transformer;

import com.example.kafkastreamsinaction.model.Purchase;
import com.example.kafkastreamsinaction.model.RewardAccumulator;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Objects;

public class PurchaseRewardTransformer implements ValueTransformer<Purchase, RewardAccumulator> {
    private KeyValueStore<String, Integer> stateStore;
    private final String storeName;
    private ProcessorContext context;

    public PurchaseRewardTransformer(String storeName) {
        this.storeName = Objects.requireNonNull(storeName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.stateStore = (KeyValueStore) this.context.getStateStore(storeName);
    }

    @Override
    public RewardAccumulator transform(Purchase value) {
        RewardAccumulator rewardAccumulator = RewardAccumulator.builder(value).build();
        Integer accumulatedSoFar = stateStore.get(rewardAccumulator.getCustomerId());

        if (accumulatedSoFar != null) {
            rewardAccumulator.addRewardPoints(accumulatedSoFar);
        }
        stateStore.put(rewardAccumulator.getCustomerId(), rewardAccumulator.getTotalRewardPoints());

        return rewardAccumulator;
    }

    @Override
    public void close() {

    }
}
