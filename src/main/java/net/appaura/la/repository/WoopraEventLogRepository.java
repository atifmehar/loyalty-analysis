package net.appaura.la.repository;

import net.appaura.la.model.WoopraEventLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface WoopraEventLogRepository extends ReactiveMongoRepository<WoopraEventLog, String> {
    Flux<WoopraEventLog> findByEventName(String eventName);
}