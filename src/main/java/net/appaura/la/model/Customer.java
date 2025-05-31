package net.appaura.la.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "customers")
public record Customer(
        @Id
        String customerId,
        String name,
        String email,
        String joinDate,
        String status // active/inactive
) {}