package net.appaura.la.controller;

import lombok.extern.log4j.Log4j2;
import net.appaura.la.model.Customer;
import net.appaura.la.model.Reward;
import net.appaura.la.model.Transaction;
import net.appaura.la.model.WoopraEventLog;
import net.appaura.la.service.LoyaltyService;
import net.appaura.la.repository.WoopraEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
@Log4j2
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final WoopraEventLogRepository woopraEventLogRepository;

    @Autowired
    public LoyaltyController(LoyaltyService loyaltyService, WoopraEventLogRepository woopraEventLogRepository) {
        this.loyaltyService = loyaltyService;
        this.woopraEventLogRepository = woopraEventLogRepository;
    }

    @PostMapping("/rewards")
    public Mono<Reward> addReward(@RequestBody Reward reward) {
        return loyaltyService.trackRedemption(reward);
    }

    @GetMapping("/active-members")
    public Flux<Customer> getActiveMembers() {
        return loyaltyService.getActiveMembers();
    }

    @GetMapping("/churn-rate")
    public Mono<ResponseEntity<Double>> getChurnRate() {
        return loyaltyService.calculateChurnRate()
                .map(rate -> ResponseEntity.ok(rate))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0.0)));
    }

    @GetMapping("/repeat-purchase-rate")
    public Mono<Double> getRepeatPurchaseRate() {
        return loyaltyService.calculateRepeatPurchaseRate();
    }

    @PostMapping("/reduce-churn")
    public Mono<Void> reduceChurn() {
        return loyaltyService.reduceChurn();
    }

    @GetMapping("/woopra-events/{eventName}")
    public Flux<WoopraEventLog> getWoopraEvents(@PathVariable String eventName) {
        return woopraEventLogRepository.findByEventName(eventName);
    }

    @PostMapping("/customers")
    public Mono<Customer> syncCustomer(@RequestBody Customer customer) {
        return loyaltyService.syncCustomerToPlatform(customer);
    }

    @GetMapping("/transactions/all")
    public Flux<Transaction> getAllTransactions() {
        return loyaltyService.getAllTransactions();
    }

    @GetMapping("/transactions/search")
    public Flux<Transaction> searchTransactions(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String timestamp,
            @RequestParam(required = false) String items,
            @RequestParam(required = false) Boolean couponUsed) {
        return loyaltyService.searchTransactions(transactionId, customerId, minAmount, maxAmount, timestamp, items, couponUsed)
                .doOnError(e -> log.error("Error searching transactions: {}", e.getMessage()))
                .onErrorResume(e -> Flux.empty()); // Return empty Flux instead of propagating the error
    }

    @GetMapping("/inactive-members")
    public Flux<Customer> getInactiveMembers() {
        return loyaltyService.getInactiveMembers();
    }

    @GetMapping("/rewards")
    public Flux<Reward> getAllRewards() {
        return loyaltyService.getAllRewards();
    }

    @GetMapping("/rewards/search")
    public Flux<Reward> searchRewards(
            @RequestParam(required = false) String rewardId,
            @RequestParam(required = false) String customerId) {
        return loyaltyService.searchRewards(rewardId, customerId);
    }

    @PostMapping("/late-night-offer")
    public Mono<Map<String, Object>> triggerLateNightOffer(@RequestBody Transaction transaction) {
        log.info("<<<< inside triggerLateNightOffer() >>>>");
        return loyaltyService.trackLateNightOffer(transaction, LocalDateTime.now())
                .doOnError(e -> log.error("Error triggering late night offer: {}", e.getMessage()))
                .onErrorResume(e -> Mono.just(Map.of(
                        "status", "error",
                        "message", "Failed to process late night offer: " + e.getMessage()
                )));
    }

    // Average Order Value (AOV)
    @GetMapping("/aov-with-coupons")
    public Mono<Double> getAverageOrderValueWithCoupons() {
        return loyaltyService.getAverageOrderValueWithCoupons();
    }

    // Average Order Value (AOV)
    @GetMapping("/aov-without-coupons")
    public Mono<Double> getAverageOrderValueWithoutCoupons() {
        return loyaltyService.getAverageOrderValueWithoutCoupons();
    }

}