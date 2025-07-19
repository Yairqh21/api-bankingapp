package com.riaydev.bankingapp.Services;

import java.math.BigDecimal;

public interface SubscriptionService {

    String createSubscription(BigDecimal amount, Integer intervalSeconds, String pin) ;

    String cancelSubscription(String pin);
}
