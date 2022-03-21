package com.example.kafkastreamsinaction.serde;

import com.example.kafkastreamsinaction.model.StockTickerData;
import org.springframework.kafka.support.serializer.JsonSerde;

public class StockTickerDataSerde extends JsonSerde<StockTickerData> {
}
