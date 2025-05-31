package net.appaura.la.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "woopra_event_logs")
public class WoopraEventLog {
    @Id
    private String id;
    private String eventName;
    private Map<String, Object> properties;
    private LocalDateTime timestamp;

    public WoopraEventLog(String eventName, Map<String, Object> properties) {
        this.id = java.util.UUID.randomUUID().toString();
        this.eventName = eventName;
        this.properties = properties;
        this.timestamp = LocalDateTime.now();
    }
}