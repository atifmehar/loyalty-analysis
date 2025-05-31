package net.appaura.la.controller;

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

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final WoopraEventLogRepository woopraEventLogRepository;

    @Autowired
    public LoyaltyController(LoyaltyService loyaltyService, WoopraEventLogRepository woopraEventLogRepository) {
        this.loyaltyService = loyaltyService;
        this.woopraEventLogRepository = woopraEventLogRepository;
    }

    @PostMapping("/transactions")
    public Mono<Transaction> addTransaction(@RequestBody Transaction transaction) {
        return loyaltyService.trackPurchase(transaction);
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

    @GetMapping("/transactions")
    public Flux<Transaction> getAllTransactions() {
        return loyaltyService.getAllTransactions();
    }

    @GetMapping("/inactive-members")
    public Flux<Customer> getInactiveMembers() {
        return loyaltyService.getInactiveMembers();
    }
}