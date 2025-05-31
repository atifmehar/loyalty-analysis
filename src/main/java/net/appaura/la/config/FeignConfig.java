package net.appaura.la.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Logger;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FeignConfig {

    @Value("${woopra.app-id}")
    private String appId;

    @Value("${woopra.app-secret}")
    private String appSecret;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(appId, appSecret);
    }

    @Bean
    public Encoder feignEncoder() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false); // Avoid extra whitespace
        return new JacksonEncoder(mapper);
    }

    @Bean
    public Decoder feignDecoder() {
        return (response, type) -> {
            try (BufferedReader reader = new BufferedReader(response.body().asReader(StandardCharsets.UTF_8))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return Mono.just(result.toString());
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Failed to decode response", e));
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // For debugging Feign requests
    }

    // In FeignConfig.java
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3); // 100ms initial backoff, 1s max backoff, 3 attempts
    }
}