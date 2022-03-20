package com.example.kafkastreamsinaction.joiner;

import com.example.kafkastreamsinaction.model.CorrelatedPurchase;
import com.example.kafkastreamsinaction.model.Purchase;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueJoinerWithKey;

import java.util.Date;
import java.util.List;

@Slf4j
public class PurchaseJoiner implements ValueJoinerWithKey<String, Purchase, Purchase, CorrelatedPurchase> {

    @Override
    public CorrelatedPurchase apply(String key, Purchase purchase, Purchase otherPurchase) {
        CorrelatedPurchase.Builder builder = CorrelatedPurchase.newBuilder();

        Date purchaseDate = purchase.getPurchaseDate() == null ? null : purchase.getPurchaseDate();
        double price = purchase.getPrice();
        String itemPurchased = purchase.getItemPurchased();

        Date otherDate = otherPurchase.getPurchaseDate() == null ? null : otherPurchase.getPurchaseDate();
        double otherPrice = otherPurchase.getPrice();
        String otherItemPurchased = otherPurchase.getItemPurchased();

        return builder
                .withCustomerId(key)
                .withFirstPurchaseDate(purchaseDate)
                .withSecondPurchaseDate(otherDate)
                .withItemsPurchased(List.of(itemPurchased, otherItemPurchased))
                .withTotalAmount(price + otherPrice)
                .build();
    }
}
