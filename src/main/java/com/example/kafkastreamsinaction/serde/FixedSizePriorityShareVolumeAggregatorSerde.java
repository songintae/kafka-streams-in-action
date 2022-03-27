package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.aggregator.FixedSizePriorityShareVolumeAggregator;
import org.springframework.kafka.support.serializer.JsonSerde;

public class FixedSizePriorityShareVolumeAggregatorSerde extends JsonSerde<FixedSizePriorityShareVolumeAggregator> {
}
