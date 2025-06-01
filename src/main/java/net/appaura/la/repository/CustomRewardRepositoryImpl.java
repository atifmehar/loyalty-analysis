package net.appaura.la.repository;

import net.appaura.la.model.Reward;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class CustomRewardRepositoryImpl implements CustomRewardRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public CustomRewardRepositoryImpl(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Flux<Reward> findByCriteria(String rewardId, String customerId) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (rewardId != null && !rewardId.isEmpty()) {
            criteriaList.add(Criteria.where("_id").is(rewardId));
        }
        if (customerId != null && !customerId.isEmpty()) {
            criteriaList.add(Criteria.where("customerId").is(customerId));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }

        return mongoTemplate.find(query, Reward.class);
    }
}
