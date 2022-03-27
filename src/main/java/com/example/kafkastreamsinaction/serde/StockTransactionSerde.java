package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.StockTransaction;
import org.springframework.kafka.support.serializer.JsonSerde;

public class StockTransactionSerde extends JsonSerde<StockTransaction> {
}
