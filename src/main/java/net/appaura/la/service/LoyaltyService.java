package net.appaura.la.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import net.appaura.la.client.LoyaltyPlatformClient;
import net.appaura.la.client.WoopraClient;
import net.appaura.la.model.Customer;
import net.appaura.la.model.Reward;
import net.appaura.la.model.Transaction;
import net.appaura.la.model.WoopraEventLog;
import net.appaura.la.repository.CustomerRepository;
import net.appaura.la.repository.RewardRepository;
import net.appaura.la.repository.TransactionRepository;
import net.appaura.la.repository.WoopraEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyService {
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyService.class);
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final RewardRepository rewardRepository;
    private final WoopraEventLogRepository woopraEventLogRepository;
    private final WoopraClient woopraClient;
    private final LoyaltyPlatformClient loyaltyPlatformClient;
    private final ReactiveMongoTemplate mongoTemplate;
    private final String DOMAIN_NAME = "appaura.net";

    public Mono<Transaction> trackPurchase(Transaction transaction) {
        return transactionRepository.save(transaction)
                .flatMap(saved -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("cv_userId", saved.customerId());
                    properties.put("cv_amount", saved.amount());
                    properties.put("cv_items", String.join(",", saved.items()));
                    properties.put("cv_couponUsed", saved.couponUsed());

                    WoopraEventLog eventLog = new WoopraEventLog("purchase", properties);
                    return woopraEventLogRepository.save(eventLog)
                            .then(woopraClient.trackEvent(
                                    DOMAIN_NAME,
                                    UUID.randomUUID().toString(),
                                    "purchase",
                                    saved.customerId(),
                                    saved.amount(),
                                    String.join(",", saved.items()),
                                    saved.couponUsed(),
                                    null,
                                    null
                            ))
                            .doOnSuccess(response -> logger.info("Woopra event response: {}", response))
                            .doOnError(e -> logger.error("Failed to track purchase event: {}", e.getMessage()))
                            .onErrorResume(FeignException.class, e -> {
                                logger.warn("Woopra API call failed, proceeding with transaction: {}", e.getMessage());
                                return Mono.just("Woopra error: " + e.getMessage());
                            })
                            .thenReturn(saved);
                });
    }

    public Mono<Reward> trackRedemption(Reward reward) {
        return rewardRepository.save(reward)
                .flatMap(saved -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("cv_userId", saved.customerId());
                    properties.put("cv_pointsRedeemed", saved.pointsRedeemed());

                    WoopraEventLog eventLog = new WoopraEventLog("redemption", properties);
                    return woopraEventLogRepository.save(eventLog)
                            .then(woopraClient.trackEvent(
                                    DOMAIN_NAME,
                                    UUID.randomUUID().toString(),
                                    "redemption",
                                    saved.customerId(),
                                    null,
                                    null,
                                    null,
                                    saved.pointsRedeemed(),
                                    null
                            ))
                            .doOnSuccess(response -> logger.info("Woopra event response: {}", response))
                            .doOnError(e -> logger.error("Failed to track redemption event: {}", e.getMessage()))
                            .onErrorResume(FeignException.class, e -> {
                                logger.warn("Woopra API call failed, proceeding with redemption: {}", e.getMessage());
                                return Mono.just("Woopra error: " + e.getMessage());
                            })
                            .thenReturn(saved);
                });
    }

    public Mono<Customer> syncCustomerToPlatform(Customer customer) {
        return customerRepository.save(customer)
                .flatMap(saved -> loyaltyPlatformClient.sendCustomer(saved)
                        .doOnSuccess(response -> logger.info("Customer synced: {}", response))
                        .doOnError(e -> logger.error("Failed to sync customer: {}", e.getMessage()))
                        .onErrorResume(FeignException.class, e -> {
                            logger.warn("Loyalty platform sync failed, proceeding: {}", e.getMessage());
                            return Mono.just("Sync error: " + e.getMessage());
                        })
                        .thenReturn(saved));
    }

    public Flux<Customer> getActiveMembers() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime thirtyDaysAgoZoned = now.minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime thirtyDaysAgo = thirtyDaysAgoZoned.toLocalDateTime();

        logger.info("Fetching active members with transactions after: {}", thirtyDaysAgo);

        Query query = new Query(Criteria.where("date").gt(thirtyDaysAgo));
        return mongoTemplate.find(query, Transaction.class)
                .doOnNext(t -> logger.debug("Transaction found by MongoTemplate: customerId={}, date={}", t.customerId(), t.date()))
                .collectList()
                .doOnNext(transactions -> logger.info("Total transactions found: {}", transactions.size()))
                .flatMapMany(transactions -> Flux.fromIterable(transactions))
                .map(Transaction::customerId)
                .distinct()
                .doOnNext(customerId -> logger.debug("Attempting to fetch customer with ID: {}", customerId))
                .flatMap(customerId -> customerRepository.findById(customerId)
                        .doOnNext(customer -> logger.debug("Found customer: id={}, name={}", customer.customerId(), customer.name()))
                        .switchIfEmpty(Mono.defer(() -> {
                            logger.warn("Customer not found for ID: {}", customerId);
                            return Mono.empty();
                        }))
                )
                .doOnComplete(() -> logger.info("Completed fetching active members"));
    }

    public Mono<Double> calculateChurnRate() {
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        return customerRepository.findAll()
                .collectList()
                .flatMap(customers -> {
                    Flux<Transaction> recentTransactions = transactionRepository.findAll()
                            .filter(t -> t.date().isAfter(ninetyDaysAgo));
                    return recentTransactions
                            .map(Transaction::customerId)
                            .distinct()
                            .collectList()
                            .map(activeIds -> {
                                long totalCustomers = customers.size();
                                long inactiveCustomers = totalCustomers - activeIds.size();
                                return totalCustomers > 0 ? (double) inactiveCustomers / totalCustomers * 100 : 0.0;
                            });
                });
    }

    public Mono<Double> calculateRepeatPurchaseRate() {
        return transactionRepository.findAll()
                .groupBy(Transaction::customerId)
                .flatMap(group -> group.count())
                .filter(count -> count > 1)
                .count()
                .zipWith(customerRepository.count())
                .map(tuple -> tuple.getT2() > 0 ? (double) tuple.getT1() / tuple.getT2() * 100 : 0.0);
    }

    public Mono<Void> reduceChurn() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return customerRepository.findAll()
                .flatMap(customer -> transactionRepository.findByCustomerId(customer.customerId())
                        .filter(t -> t.date().isAfter(thirtyDaysAgo))
                        .collectList()
                        .flatMap(transactions -> transactions.isEmpty() ? Mono.just(customer) : Mono.empty())
                )
                .doOnNext(customer -> logger.info("Identified inactive customer: {}", customer.customerId()))
                .flatMap(customer -> {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("cv_userId", customer.customerId());
                    properties.put("cv_offer", "20% off next purchase");

                    WoopraEventLog eventLog = new WoopraEventLog("offer_sent", properties);
                    return woopraEventLogRepository.save(eventLog)
                            .doOnSuccess(log -> logger.info("Logged offer_sent event for customer: {}", customer.customerId()))
                            .then(woopraClient.trackEvent(
                                    DOMAIN_NAME,
                                    UUID.randomUUID().toString(),
                                    "offer_sent",
                                    customer.customerId(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    "20% off next purchase"
                            ))
                            .doOnSuccess(response -> logger.info("Woopra event response for customer {}: {}", customer.customerId(), response))
                            .doOnError(e -> logger.error("Failed to track offer event for customer {}: {}", customer.customerId(), e.getMessage()))
                            .onErrorResume(FeignException.class, e -> {
                                logger.warn("Woopra API call failed for customer {}, proceeding: {}", customer.customerId(), e.getMessage());
                                return Mono.just("Woopra error: " + e.getMessage());
                            })
                            .then(loyaltyPlatformClient.sendOffer(Map.of("customerId", customer.customerId(), "offer", "20% off next purchase"))
                                    .doOnSuccess(response -> logger.info("Offer sent to customer {}: {}", customer.customerId(), response))
                                    .doOnError(e -> logger.error("Failed to send offer to customer {}: {}", customer.customerId(), e.getMessage()))
                                    .onErrorResume(FeignException.class, e -> {
                                        logger.warn("Loyalty platform offer send failed for customer {}: {}", customer.customerId(), e.getMessage());
                                        return Mono.just("Offer send error: " + e.getMessage());
                                    }));
                })
                .doOnComplete(() -> logger.info("Completed reduce-churn operation"))
                .doOnError(e -> logger.error("Error in reduce-churn operation: {}", e.getMessage()))
                .then();
    }

    public Flux<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Flux<Customer> getInactiveMembers() {
        return customerRepository.findByStatusFalse(); // Assuming a method to find inactive customers
    }
}