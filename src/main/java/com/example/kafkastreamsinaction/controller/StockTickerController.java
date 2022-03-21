package com.example.kafkastreamsinaction.controller;


import com.example.kafkastreamsinaction.model.StockTickerData;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/stock-ticker")
@RequiredArgsConstructor
public class StockTickerController {

    private final KafkaTemplate<String, StockTickerData> kafkaTemplate;
    private final InteractiveQueryService interactiveQueryService;

    @PostMapping
    public void createStockTickers() throws InterruptedException {
        for (StockTickerData stockTickerData : getDummyData()) {
            kafkaTemplate.send("stock-ticker-stream", stockTickerData.getSymbol(), stockTickerData);
            kafkaTemplate.send("stock-ticker-table", stockTickerData.getSymbol(), stockTickerData);
            Thread.sleep(1000);
        }
    }

    @GetMapping("/symbols")
    public List<StockTickerData> getPrice() {
        ReadOnlyKeyValueStore<String, StockTickerData> queryableStore = interactiveQueryService.getQueryableStore("stock-ticker-store", QueryableStoreTypes.<String, StockTickerData>keyValueStore());

        List<StockTickerData> result = new ArrayList<>();
        /**
         * KeyValueIterator는 항상 Close를 해야한다.
         */
        try (KeyValueIterator<String, StockTickerData> iterator = queryableStore.all()) {
            iterator.forEachRemaining((element) -> result.add(element.value));
        }
        return result;
    }

    @GetMapping("/symbols/{symbol}")
    public StockTickerData getPrice(@PathVariable String symbol) {
        ReadOnlyKeyValueStore<String, StockTickerData> queryableStore = interactiveQueryService.getQueryableStore("stock-ticker-store", QueryableStoreTypes.<String, StockTickerData>keyValueStore());
        return queryableStore.get(symbol);
    }

    private List<StockTickerData> getDummyData() {
        return List.of(
                new StockTickerData(105.25, "YERB"),
                new StockTickerData(53.19, "AUNA"),
                new StockTickerData(91.97, "NDLE"),
                new StockTickerData(105.74, "YERB"),
                new StockTickerData(53.78, "AUNA"),
                new StockTickerData(92.53, "NDLE"),
                new StockTickerData(106.67, "YERB"),
                new StockTickerData(54.4, "AUNA"),
                new StockTickerData(92.77, "NDLE")
        );
    }
}
