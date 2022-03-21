package com.example.kafkastreamsinaction.ktable;


import com.example.kafkastreamsinaction.model.StockTickerData;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.BiConsumer;

@Configuration
public class KStreamsVsKTableApplicationConfiguration {

    @Bean
    public BiConsumer<KStream<String, StockTickerData>, KTable<String, StockTickerData>> kStreamsVsKTableApplication() {
        return (kStream, kTable) -> {

            kStream.print(Printed.<String, StockTickerData>toSysOut()
                    .withLabel("Stocks-KStream"));
            kTable.toStream()
                    .print(Printed.<String, StockTickerData>toSysOut()
                            .withLabel("Stocks-KTable"));
        };
    }
}
