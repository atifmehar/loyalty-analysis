package net.appaura.la.repository;

import net.appaura.la.model.Transaction;
import reactor.core.publisher.Flux;

public interface CustomTransactionRepository {
    Flux<Transaction> findByCriteria(
            String transactionId,
            String customerId,
            Double minAmount,
            Double maxAmount,
            String timestamp,
            String items,
            Boolean couponUsed);
}
