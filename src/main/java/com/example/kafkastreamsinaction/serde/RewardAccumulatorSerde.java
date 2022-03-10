package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.RewardAccumulator;
import org.springframework.kafka.support.serializer.JsonSerde;

public class RewardAccumulatorSerde extends JsonSerde<RewardAccumulator> {
}
