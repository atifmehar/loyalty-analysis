package net.appaura.la.repository;

import net.appaura.la.model.Reward;
import reactor.core.publisher.Flux;

public interface CustomRewardRepository {
    Flux<Reward> findByCriteria(String rewardId, String customerId);
}
