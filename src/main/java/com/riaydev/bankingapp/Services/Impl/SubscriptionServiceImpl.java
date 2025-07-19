package com.riaydev.bankingapp.Services.Impl;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Transaction;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.TransactionRepository;
import com.riaydev.bankingapp.Services.SecurityService;
import com.riaydev.bankingapp.Services.SubscriptionService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final String SUBSCRIPTION_SUCCESS = "Subscription created successfully.";
    private static final String SUBSCRIPTION_CANCELLED = "Subscription cancelled successfully.";
    private static final String NO_ACTIVE_SUBSCRIPTION = "No active subscription found for this account.";
    private static final String ACCOUNT_NOT_FOUND = "Account not found";

    private final SecurityService securityService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Scheduler
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Long, ScheduledFuture<?>> subscriptionTasks = new ConcurrentHashMap<>();

    @Override
    public String createSubscription(BigDecimal amount, Integer intervalSeconds, String pin) {
        securityService.verifyPin(pin);

        User currentUser = securityService.getCurrentAuthenticatedUser();
        Account account = getAccountByUser(currentUser);

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(
                () -> processSubscriptionPayment(account.getId(), amount),
                intervalSeconds,
                intervalSeconds,
                TimeUnit.SECONDS);

        subscriptionTasks.put(account.getId(), task);

        log.info("Subscription created for user {}: {} every {} seconds",
                currentUser.getEmail(), amount, intervalSeconds);

        return SUBSCRIPTION_SUCCESS;
    }

    @Override
    public String cancelSubscription(String pin) {
        securityService.verifyPin(pin);
        User currentUser = securityService.getCurrentAuthenticatedUser();
        Account account = getAccountByUser(currentUser);

        ScheduledFuture<?> task = subscriptionTasks.get(account.getId());
        if (task == null) {
            return NO_ACTIVE_SUBSCRIPTION;
        }

        task.cancel(false);
        subscriptionTasks.remove(account.getId());
        log.info("Subscription manually cancelled for account {}", account.getId());
        return SUBSCRIPTION_CANCELLED;
    }

    @Async
    @Transactional
    protected void processSubscriptionPayment(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(ACCOUNT_NOT_FOUND));

        if (account.getBalance().compareTo(amount) < 0) {
            handleInsufficientFunds(accountId);
            return;
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        createSubscriptionTransaction(account, amount);
        log.info("Processed subscription payment of {} for account {}", amount, account.getId());
    }

    private Account getAccountByUser(User user) {
        return accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException(ACCOUNT_NOT_FOUND));
    }

    private void handleInsufficientFunds(Long accountId) {
        log.warn("Insufficient funds for account {}. Cancelling subscription...", accountId);
        ScheduledFuture<?> task = subscriptionTasks.get(accountId);
        if (task != null) {
            task.cancel(false);
            subscriptionTasks.remove(accountId);
            log.info("Subscription cancelled for account {}", accountId);
        }
    }

    private void createSubscriptionTransaction(Account account, BigDecimal amount) {
        Transaction transaction = Transaction.builder()
                .sourceAccount(account)
                .amount(amount)
                .transactionType(TransactionType.SUBSCRIPTION)
                .transactionDate(LocalDateTime.now())
                .targetAccount(null)
                .build();
        transactionRepository.save(transaction);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
    }
}