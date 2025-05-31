package net.appaura.la.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "transactions")
public record Transaction(
    @Id String transactionId,
    String customerId,
    double amount,
    LocalDateTime date,
    List<String> items,
    String couponUsed
) {}