package net.appaura.la.repository;

import net.appaura.la.model.Reward;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface RewardRepository extends ReactiveMongoRepository<Reward, String> {
    Flux<Reward> findByCustomerId(String customerId);
}