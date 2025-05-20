package com.mdev.chatcord.server.device.service;

import com.mdev.chatcord.server.device.dto.LocationResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class IpLocationService {

    private final WebClient webClient = WebClient.create("http://ip-api.com");

    public LocationResponse getLocation(String ip) {
        return webClient.get()
                .uri("/json/" + ip)
                .retrieve()
                .bodyToMono(LocationResponse.class)
                .doOnNext(location -> {
                    log.info("Country: {}", location.getCountry());
                    log.info("City: {}", location.getCity());
                })
                .block(); // Or use subscribe if you want async
    }

}