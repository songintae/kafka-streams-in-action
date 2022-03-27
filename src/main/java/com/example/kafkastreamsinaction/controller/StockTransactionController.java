package com.example.kafkastreamsinaction.controller;

import com.example.kafkastreamsinaction.model.StockTransaction;
import com.example.kafkastreamsinaction.utils.datagen.DataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
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
}
