package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.Purchase;
import org.springframework.kafka.support.serializer.JsonSerde;

public class PurchaseSerde extends JsonSerde<Purchase> {
}
