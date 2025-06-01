package net.appaura.la.repository;

import net.appaura.la.model.Customer;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {
    Flux<Customer> findByStatus(String status);
    @Query("{ 'status': 'Inactive' }")
    Flux<Customer> findByStatusInactive();
}