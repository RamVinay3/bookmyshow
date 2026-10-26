package com.example.booking_service.config;

import io.netty.channel.ChannelOption;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration

public class WebClientConfig {

    @LoadBalanced
    @Bean
    public WebClient.Builder webClientBuilder() {

        /*Booking Service tries to establish a TCP connection to Movie Service.
        If the connection cannot be established within the configured limit, the attempt fails.*/
        HttpClient http = HttpClient.create().
                          option(ChannelOption.CONNECT_TIMEOUT_MILLIS,2000).
                          responseTimeout(Duration.ofSeconds(3));/*After sending the request, Booking Service waits for response data.
                                                                  If the configured response-read timeout is exceeded, the request fails.*/

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(http));
    }
}
