package com.riaydev.bankingapp.Services.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.riaydev.bankingapp.Exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MarketPriceClient {

    @Value("${market.api.url}")
    private String marketApiUrl;

    private final RestTemplate restTemplate;

    public Map<String, BigDecimal> getMarketPrices() {
        ResponseEntity<Map<String, BigDecimal>> response = restTemplate.exchange(
                marketApiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, BigDecimal>>() {
                });
        return response.getBody();
    }

    public BigDecimal getAssetPrice(String assetSymbol) {
        Map<String, BigDecimal> marketPrices = getMarketPrices();
        BigDecimal price = marketPrices.get(assetSymbol);
        if (price == null) {
            throw new ResourceNotFoundException("Price not found for asset symbol: " + assetSymbol);
        }
        return price;
    }
    
}
