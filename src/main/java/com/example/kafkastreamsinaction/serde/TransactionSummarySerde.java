package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.TransactionSummary;
import org.springframework.kafka.support.serializer.JsonSerde;

public class TransactionSummarySerde extends JsonSerde<TransactionSummary> {
}
