package net.appaura.la.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Transaction(
        String transactionId,
        String customerId,
        double amount,
        LocalDateTime date,
        List<String> items,
        String couponUsed // Changed from boolean to String to match Mockfly response
) {}