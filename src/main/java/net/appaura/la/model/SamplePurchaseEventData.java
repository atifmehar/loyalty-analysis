package net.appaura.la.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SamplePurchaseEventData {
    public static WoopraEventLog getSamplePurchaseEvent() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("cv_userId", "c2");
        properties.put("cv_amount", 99.99);
        properties.put("cv_items", "item1,item2");
        properties.put("cv_couponUsed", true);

        return new WoopraEventLog(
                "purchase",
                properties
        );
    }

    public static Transaction getSampleTransaction() {
        return new Transaction(
                "TXN123",
                "c2",
                99.99,
                LocalDateTime.now(),
                Arrays.asList("item1", "item2"),
                null
        );
    }
}
