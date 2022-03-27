package com.example.kafkastreamsinaction.window;

import com.example.kafkastreamsinaction.model.StockTransaction;
import com.example.kafkastreamsinaction.model.TransactionSummary;
import com.example.kafkastreamsinaction.serde.StockTransactionSerde;
import com.example.kafkastreamsinaction.serde.TransactionSummarySerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Consumer;

@Configuration
public class CountingWindowingAndKTableJoinApplicationConfiguration {

    private final static Serde<StockTransaction> stockTransactionSerde = new StockTransactionSerde();
    private final static Serde<TransactionSummary> transactionSummarySerde = new TransactionSummarySerde();

    @Bean
    public Consumer<KStream<String, StockTransaction>> countingWindowingAndKTableJoinApplication() {
        return input -> {
            Duration twentySeconds = Duration.ofSeconds(20);
            Duration fifteenMinutes = Duration.ofMinutes(15);

            KTable<Windowed<TransactionSummary>, Long> customerTransactionCounts = input
                    .groupBy((key, value) -> TransactionSummary.from(value),
                            Grouped.with(transactionSummarySerde, stockTransactionSerde))
                    .windowedBy(SessionWindows.ofInactivityGapAndGrace(twentySeconds, fifteenMinutes))
                    .count();

            customerTransactionCounts.toStream()
                    .print(Printed.<Windowed<TransactionSummary>, Long>toSysOut()
                            .withLabel("Customer Transactions Counts"));

        };
    }
}
