package net.appaura.la.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "rewards")
public record Reward(
    @Id String rewardId,
    String customerId,
    int pointsEarned,
    int pointsRedeemed,
    LocalDateTime date
) {}