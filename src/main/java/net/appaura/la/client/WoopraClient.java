package net.appaura.la.client;

import net.appaura.la.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@FeignClient(name = "woopra", url = "${woopra.base-url}", configuration = FeignConfig.class)
public interface WoopraClient {
    @GetMapping(value = "/track/ce")
    Mono<String> trackEvent(
            @RequestParam("website") String website,
            @RequestParam("cookie") String cookie,
            @RequestParam("event") String event,
            @RequestParam("cv_userId") String userId,
            @RequestParam(value = "cv_amount", required = false) Double amount,
            @RequestParam(value = "cv_items", required = false) String items,
            @RequestParam(value = "cv_couponUsed", required = false) String couponUsed,
            @RequestParam(value = "cv_pointsRedeemed", required = false) Integer pointsRedeemed,
            @RequestParam(value = "cv_offer", required = false) String offer
    );
}