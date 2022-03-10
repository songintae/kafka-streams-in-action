package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.PurchasePattern;
import org.springframework.kafka.support.serializer.JsonSerde;

public class PurchasePatternSerde extends JsonSerde<PurchasePattern> {
}
