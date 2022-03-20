package com.example.kafkastreamsinaction.timestamp;

import com.example.kafkastreamsinaction.model.Purchase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp;


public class TransactionTimestampExtractor extends LogAndSkipOnInvalidTimestamp {
    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        Purchase purchase = (Purchase) record.value();
        long timestamp = purchase.getPurchaseDate().getTime();
        if (timestamp < 0) {
            return onInvalidTimestamp(record, timestamp, partitionTime);
        }
        return timestamp;
    }
}
