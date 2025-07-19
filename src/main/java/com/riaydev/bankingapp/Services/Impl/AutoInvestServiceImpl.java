package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Asset;
import com.riaydev.bankingapp.Entities.User;
import com.riaydev.bankingapp.Exceptions.ResourceNotFoundException;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Repositories.AccountRepository;
import com.riaydev.bankingapp.Repositories.AssetRepository;
import com.riaydev.bankingapp.Services.AutoInvestService;
import com.riaydev.bankingapp.Services.SecurityService;
import com.riaydev.bankingapp.Services.client.MarketPriceClient;
import com.riaydev.bankingapp.Services.helper.TransactionHelper;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoInvestServiceImpl implements AutoInvestService {

    // mensajes
    private static final String AUTO_INVEST_SUCCESS_MESSAGE = "Auto-invest enabled successfully.";
    private static final String AUTO_INVEST_DISABLED_MESSAGE = "Auto-invest disabled successfully.";
    private static final String AUTO_INVEST_TASK_NOT_FOUND = "No auto-invest task running for this user.";

    // Dependencias
    private final SecurityService securityService;
    private final AccountRepository accountRepository;
    private final AssetRepository assetRepository;
    private final MarketPriceClient marketPriceClient;
    private final TransactionHelper transactionHelper;

    // Componentes para ejecución programada
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Long, ScheduledFuture<?>> autoInvestTasks = new ConcurrentHashMap<>();

    // ========== MÉTODOS PÚBLICOS ==========

    @Override
    @Transactional
    public String enableAutoInvest(String pin) {
        securityService.verifyPin(pin);
        User currentUser = securityService.getCurrentAuthenticatedUser();
        Long userId = currentUser.getId();

        cancelExistingTask(userId);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            () -> executeAutoInvestStrategy(currentUser),
            30, 30, TimeUnit.SECONDS);

        autoInvestTasks.put(userId, future);

        log.info("Auto-invest enabled for user {}", currentUser.getEmail());
        return AUTO_INVEST_SUCCESS_MESSAGE;
    }

    @Override
    @Transactional
    public String disableAutoInvest() {
        User currentUser = securityService.getCurrentAuthenticatedUser();
        Long userId = currentUser.getId();

        ScheduledFuture<?> task = autoInvestTasks.get(userId);
        if (task != null) {
            task.cancel(true);
            autoInvestTasks.remove(userId);
            log.info("Auto-invest disabled for user {}", currentUser.getEmail());
            return AUTO_INVEST_DISABLED_MESSAGE;
        }

        return AUTO_INVEST_TASK_NOT_FOUND;
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
    }

    // ========== MÉTODOS PRIVADOS DE IMPLEMENTACIÓN ==========

    @Async
    @Transactional
    protected void executeAutoInvestStrategy(User user) {
        try {
            Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
                
            List<Asset> userAssets = assetRepository.findByUser(user);

            if (userAssets.isEmpty()) {
                log.info("No assets found for user {}", user.getEmail());
                return;
            }

            Map<String, BigDecimal> currentPrices = marketPriceClient.getMarketPrices();
            evaluateAssets(userAssets, currentPrices, account);
        } catch (Exception e) {
            log.error("Error executing auto-invest strategy for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void evaluateAssets(List<Asset> userAssets, Map<String, BigDecimal> currentPrices, Account account) {
        for (Asset asset : userAssets) {
            BigDecimal currentPrice = currentPrices.get(asset.getAssetSymbol());
            if (currentPrice == null) continue;

            BigDecimal priceChange = calculatePriceChange(currentPrice, asset.getPurchasePrice());

            if (priceChange.compareTo(BigDecimal.valueOf(-20)) < 0) {
                buyMoreAsset(account, asset, currentPrice);
            } else if (priceChange.compareTo(BigDecimal.valueOf(20)) > 0) {
                sellSomeAsset(account, asset, currentPrice);
            }
        }
    }

    private BigDecimal calculatePriceChange(BigDecimal currentPrice, BigDecimal purchasePrice) {
        return currentPrice.subtract(purchasePrice)
                .divide(purchasePrice, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private void buyMoreAsset(Account account, Asset existingAsset, BigDecimal currentPrice) {
        BigDecimal amountToInvest = calculateInvestmentAmount(account.getBalance());
        
        if (amountToInvest.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Insufficient balance for auto-invest: {}", amountToInvest);
            return;
        }

        BigDecimal quantity = amountToInvest.divide(currentPrice, 8, RoundingMode.HALF_UP);
        updateAsset(quantity, existingAsset, currentPrice);

        account.setBalance(account.getBalance().subtract(amountToInvest));
        accountRepository.save(account);

        transactionHelper.createTransaction(account, amountToInvest, TransactionType.ASSET_PURCHASE);
        log.info("Auto-invest: bought {} more of {} at {}", quantity, existingAsset.getAssetSymbol(), currentPrice);
    }

    private BigDecimal calculateInvestmentAmount(BigDecimal balance) {
        BigDecimal amountToInvest = balance.multiply(BigDecimal.valueOf(0.1)); // 10% del balance
        BigDecimal minimumBalance = BigDecimal.valueOf(100);
        BigDecimal availableBalance = balance.subtract(minimumBalance);

        return availableBalance.compareTo(amountToInvest) < 0 ? availableBalance : amountToInvest;
    }

    private void updateAsset(BigDecimal quantity, Asset existingAsset, BigDecimal currentPrice) {
        BigDecimal oldQuantity = existingAsset.getQuantity();
        BigDecimal newQuantity = oldQuantity.add(quantity);

        BigDecimal weightedPrice = existingAsset.getPurchasePrice()
                .multiply(oldQuantity)
                .add(currentPrice.multiply(quantity))
                .divide(newQuantity, 2, RoundingMode.HALF_UP);

        existingAsset.setPurchasePrice(weightedPrice);
        existingAsset.setQuantity(newQuantity);
        assetRepository.save(existingAsset);
    }

    private void sellSomeAsset(Account account, Asset existingAsset, BigDecimal currentPrice) {
        if (existingAsset.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Cannot sell asset {}: no quantity available", existingAsset.getAssetSymbol());
            return;
        }

        BigDecimal quantityToSell = calculateSellQuantity(existingAsset.getQuantity());
        BigDecimal amountEarned = quantityToSell.multiply(currentPrice);

        existingAsset.setQuantity(existingAsset.getQuantity().subtract(quantityToSell));
        assetRepository.save(existingAsset);

        account.setBalance(account.getBalance().add(amountEarned));
        accountRepository.save(account);

        transactionHelper.createTransaction(account, amountEarned, TransactionType.ASSET_SELL);
        log.info("Auto-invest: sold {} of {} at {}", quantityToSell, existingAsset.getAssetSymbol(), currentPrice);
    }

    private BigDecimal calculateSellQuantity(BigDecimal currentQuantity) {
        BigDecimal quantityToSell = currentQuantity.multiply(BigDecimal.valueOf(0.25));
        return quantityToSell.compareTo(currentQuantity) > 0 ? currentQuantity : quantityToSell;
    }

    private void cancelExistingTask(Long userId) {
        if (autoInvestTasks.containsKey(userId)) {
            autoInvestTasks.get(userId).cancel(true);
            autoInvestTasks.remove(userId);
        }
    }
}