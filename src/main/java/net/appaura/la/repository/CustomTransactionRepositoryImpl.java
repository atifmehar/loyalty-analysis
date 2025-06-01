package net.appaura.la.repository;

import net.appaura.la.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomTransactionRepositoryImpl.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ReactiveMongoTemplate mongoTemplate;

    public CustomTransactionRepositoryImpl(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Flux<Transaction> findByCriteria(
            String transactionId,
            String customerId,
            Double minAmount,
            Double maxAmount,
            String timestamp,
            String items,
            Boolean couponUsed) {
        List<Criteria> criteriaList = new ArrayList<>();

        logger.debug("Received search parameters: transactionId={}, customerId={}, minAmount={}, maxAmount={}, timestamp={}, items={}, couponUsed={}",
                transactionId, customerId, minAmount, maxAmount, timestamp, items, couponUsed);

        if (transactionId != null && !transactionId.isEmpty()) {
            criteriaList.add(Criteria.where("_id").is(transactionId));
        }
        if (customerId != null && !customerId.isEmpty()) {
            criteriaList.add(Criteria.where("customerId").is(customerId));
        }
        if (minAmount != null) {
            criteriaList.add(Criteria.where("amount").gte(minAmount));
        }
        if (maxAmount != null) {
            criteriaList.add(Criteria.where("amount").lte(maxAmount));
        }
        if (timestamp != null && !timestamp.isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER);
                criteriaList.add(Criteria.where("date").is(dateTime));
                logger.debug("Parsed timestamp: {}", dateTime);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid timestamp format: {}. Expected format: yyyy-MM-dd'T'HH:mm:ss", timestamp);
                // Instead of throwing an error, we'll skip this criterion
            }
        }
        if (items != null && !items.isEmpty()) {
            criteriaList.add(Criteria.where("items").in(items.split(",")));
        }
        if (couponUsed != null) {
            criteriaList.add(Criteria.where("couponUsed").is(couponUsed));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }

        logger.debug("Executing query: {}", query.getQueryObject());

        return mongoTemplate.find(query, Transaction.class)
                .doOnError(e -> logger.error("Failed to fetch transactions: {}", e.getMessage()))
                .doOnComplete(() -> logger.info("Transaction fetch completed"));
    }
}
