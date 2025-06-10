package net.appaura.la.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Logger;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
    public Decoder feignDecoder(ObjectMapper mapper) {
        return (response, type) -> {
            try {
                if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(reactor.core.publisher.Mono.class)) {
                    Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    Object result = mapper.readValue(response.body().asInputStream(), mapper.getTypeFactory().constructType(genericType));
                    return reactor.core.publisher.Mono.just(result); // Explicitly wrap in Mono
                }
                return mapper.readValue(response.body().asInputStream(), mapper.getTypeFactory().constructType(type));
            } catch (IOException e) {
                throw new RuntimeException("Failed to decode response", e);
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // For debugging Feign requests
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3); // 100ms initial backoff, 1s max backoff, 3 attempts
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false); // Avoid extra whitespace
        return mapper;
    }
}