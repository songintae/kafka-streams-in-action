package com.example.kafkastreamsinaction.controller;

import com.example.kafkastreamsinaction.model.StockTransaction;
import com.example.kafkastreamsinaction.utils.datagen.DataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/stock-transaction")
@RequiredArgsConstructor
public class StockTransactionController {
    private final KafkaTemplate<String, StockTransaction> kafkaTemplate;

    @PostMapping
    public void createTransaction() {
        kafkaTemplate.send("stock-transaction", DataGenerator.generateStockTransaction());
    }

    /**
     * customerId list : "12345678", "222333444", "33311111", "55556666", "4488990011", "77777799", "111188886", "98765432", "665552228", "660309116"
     * symbol list : "AEBB", "VABC", "ALBC", "EABC", "BWBC", "BNBC", "MASH", "BARX", "WNBC", "WKRP"
     *
     * @param customerId
     * @param symbol
     */
    @PostMapping("/{customerId}/{symbol}")
    public void createTransaction(@PathVariable String customerId, @PathVariable String symbol) {
        kafkaTemplate.send("stock-transaction", DataGenerator.generateStockTransaction(customerId, symbol));
    }
}
