package com.example.kafkastreamsinaction.ktable;


import com.example.kafkastreamsinaction.aggregator.FixedSizePriorityShareVolumeAggregator;
import com.example.kafkastreamsinaction.model.ShareVolume;
import com.example.kafkastreamsinaction.model.StockTransaction;
import com.example.kafkastreamsinaction.serde.FixedSizePriorityShareVolumeAggregatorSerde;
import com.example.kafkastreamsinaction.serde.ShareVolumeSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class AggregationAndReducingApplicationConfiguration {
    private static final Serde<String> stringSerde = Serdes.String();
    private static final Serde<ShareVolume> shareVolumeSerde = new ShareVolumeSerde();
    private static final FixedSizePriorityShareVolumeAggregatorSerde fixedSizePriorityQueueSerde = new FixedSizePriorityShareVolumeAggregatorSerde();
    private static final Comparator<ShareVolume> comparator = (sv1, sv2) -> sv2.getShares() - sv1.getShares();
    private static final NumberFormat numberFormat = NumberFormat.getInstance();


    @Bean
    public Consumer<KStream<String, StockTransaction>> aggregationAndReducingApplication() {
        return (kStream) -> {
            KTable<String, ShareVolume> shareVolume = kStream
                    .mapValues((key, value) -> ShareVolume.newBuilder(value).build())
                    .groupBy((key, value) -> value.getSymbol(), Grouped.with(stringSerde, shareVolumeSerde))
                    .reduce(ShareVolume::sum);


            ValueMapper<FixedSizePriorityShareVolumeAggregator, String> valueMapper = value -> {
                StringBuilder builder = new StringBuilder();
                Iterator<ShareVolume> iterator = value.iterator();
                int counter = 1;
                while (iterator.hasNext()) {
                    ShareVolume stockVolume = iterator.next();
                    if (stockVolume != null) {
                        builder.append(counter++).append(")").append((stockVolume.getSymbol()))
                                .append(":").append(numberFormat.format(stockVolume.getShares())).append(" ");
                    }
                }
                return builder.toString();
            };
            shareVolume
                    .groupBy((key, value) -> KeyValue.pair(value.getIndustry(), value), Grouped.with(stringSerde, shareVolumeSerde))
                    .aggregate(() -> new FixedSizePriorityShareVolumeAggregator(comparator, 5),
                            (key, value, aggregate) -> aggregate.add(value),
                            (key, value, aggregate) -> aggregate.remove(value),
                            Materialized.<String, FixedSizePriorityShareVolumeAggregator, KeyValueStore<Bytes, byte[]>>as("industry-priority-share-volume-store")
                                    .withKeySerde(stringSerde)
                                    .withValueSerde(fixedSizePriorityQueueSerde))
                    .mapValues(valueMapper)
                    .toStream()
                    .peek((key, value) -> log.info("Stock volume by industry {} {}", key, value))
                    .to("stock-volume-by-company", Produced.with(stringSerde, stringSerde));
        };
    }
}
