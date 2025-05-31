package net.appaura.la.repository;

import net.appaura.la.model.Transaction;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByCustomerId(String customerId);

    @Query("{ 'date': { $gt: ?0 } }")
    Flux<Transaction> findByDateAfter(LocalDateTime date);
}