package com.riaydev.bankingapp.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riaydev.bankingapp.Services.MarketService;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService){
        this.marketService = marketService;
    }

    @GetMapping("/prices")
    public ResponseEntity<?> getMarketPrices() {
        try {
            Map<String, BigDecimal> prices = marketService.getMarketPrices();
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("msg", "Error fetching market prices."));
        }
    }

    @GetMapping("/prices/{symbol}")
    public ResponseEntity<BigDecimal> getAssetPrice(@Valid @PathVariable String symbol) throws Exception {
        return ResponseEntity.ok(marketService.getAssetPrice(symbol));
    }

}
