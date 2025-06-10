package net.appaura.la.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.appaura.la.repository.TransactionRepository;
import net.appaura.la.service.LoyaltyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Log4j2
public class LateNightOfferSchedule {
    private final TransactionRepository transactionRepository;
    private final LoyaltyService loyaltyService;

  //  @Scheduled(cron = "0 0 22-23,0-1 * * ?")// Runs every hour
    public Mono<Void> scheduleLateNightOffer() {
        log.info("<<<< inside scheduleLateNightOffer() >>>>");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        int hour = now.getHour();

        if (hour >= 22 || hour < 2) { // 10:00 PM to 2:00 AM PKT
            return loyaltyService.getAllTransactions()
                    .flatMap(transaction -> loyaltyService.trackLateNightOffer(transaction, now)
                            .doOnSuccess(result -> log.info("Late night offer processed for transaction {}: {}", transaction.transactionId(), result))
                            .doOnError(e -> log.error("Error processing offer for transaction {}: {}", transaction.transactionId(), e.getMessage()))
                            .then())
                    .then();
        }
        return Mono.empty(); // Do nothing outside the scheduled hours
    }
}
