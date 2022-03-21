package com.example.kafkastreamsinaction.controller;

import com.example.kafkastreamsinaction.model.Purchase;
import com.example.kafkastreamsinaction.utils.datagen.DataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final KafkaTemplate<String, Purchase> kafkaTemplate;

    @PostMapping
    public void createPurchase() {
        kafkaTemplate.send("transaction", null, DataGenerator.generatePurchase());
    }

    @PostMapping("/coffee")
    public void createCoffeePurchase() {
        kafkaTemplate.send("transaction", null, DataGenerator.generateCoffeePurchase("customerId"));
    }

    @PostMapping("/electronics")
    public void createElectronicsPurchase() {
        kafkaTemplate.send("transaction", null, DataGenerator.generateElectronicPurchase("customerId"));
    }

}
