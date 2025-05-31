package net.appaura.la.client;

import net.appaura.la.config.FeignConfig;
import net.appaura.la.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.Map;

@FeignClient(name = "loyalty-platform", url = "https://api.mockfly.dev/mocks/897e33fb-fb39-4c89-a14b-341a4b1378a9", configuration = FeignConfig.class)
public interface LoyaltyPlatformClient {
    @PostMapping(value = "/customers", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> sendCustomer(@RequestBody Customer customer);

    @PostMapping(value = "/offers", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<String> sendOffer(@RequestBody Map<String, String> offer);
}