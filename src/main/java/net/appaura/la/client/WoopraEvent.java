package net.appaura.la.client;

import lombok.Data;

import java.util.Map;

@Data
public class WoopraEvent {
    private String website; // Woopra project host (e.g., "loyalty-analysis")
    private String cookie; // Unique session identifier
    private String event; // Event name (e.g., "purchase")
    private Map<String, Object> cv_properties; // Custom properties with "cv_" prefix
}