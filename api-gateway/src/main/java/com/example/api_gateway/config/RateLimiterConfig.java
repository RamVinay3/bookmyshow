package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange ->
                reactor.core.publisher.Mono.justOrEmpty(
                                exchange.getRequest()
                                        .getRemoteAddress()
                        )
                        .map(address ->
                                address.getAddress().getHostAddress()
                        );
    }
}
