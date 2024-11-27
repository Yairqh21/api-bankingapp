package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.riaydev.bankingapp.DTO.BuyAssetRequest;
import com.riaydev.bankingapp.DTO.SellAssetRequest;
import com.riaydev.bankingapp.Entities.*;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Exceptions.*;
import com.riaydev.bankingapp.Repositories.*;
import com.riaydev.bankingapp.Services.EmailService;
import com.riaydev.bankingapp.Services.MarketService;
import com.riaydev.bankingapp.Services.SecurityService;

import jakarta.transaction.Transactional;

@Service
public class MarketServiceImpl implements MarketService {

        @Autowired
        private AssetRepository assetRepository;
        @Autowired
        private AccountRepository accountRepository;
        @Autowired
        private EmailService emailService;
        @Autowired
        private SecurityService securityService;
        @Autowired
        private TransactionRepository transactionRepository;

        @Value("${market.api.url}")
        private String marketApiUrl;

        @Override
        @Transactional
        public void buyAsset(BuyAssetRequest request) throws Exception {

                securityService.verifyPin(request.pin());
                
                BigDecimal assetCurrentPrice = getAssetPrice(request.assetSymbol());

                User user = securityService.getCurrentAuthenticatedUser();

                Account account = securityService.getAccountForCurrentUser();

                securityService.validateSufficientBalance(account, request.amount());

                //Comprar el activo
                BigDecimal assetQuantity = request.amount().divide(assetCurrentPrice, RoundingMode.HALF_UP);
                Asset asset = assetRepository.findByUserAndAssetSymbol(user, request.assetSymbol()).orElse(null);

                if (asset == null) {
                        
                        asset = Asset.builder()
                                        .user(user)
                                        .assetSymbol(request.assetSymbol())
                                        .purchasePrice(assetCurrentPrice) 
                                        .quantity(assetQuantity)
                                        .build();
                } else {
                        // Calcular precio promedio ponderado para el activo existente
                        BigDecimal totalInvestment = asset.getPurchasePrice().multiply(asset.getQuantity())
                                        .add(request.amount());
                        BigDecimal totalQuantity = asset.getQuantity().add(assetQuantity);
                        BigDecimal avgPriceAsset = totalInvestment.divide(totalQuantity, RoundingMode.HALF_UP);

                        // Actualizar el activo con la nueva cantidad y precio promedio
                        asset.setQuantity(totalQuantity);
                        asset.setPurchasePrice(avgPriceAsset); // Precio promedio actualizado
                }

                assetRepository.save(asset);

                // Guardar el historial de transacción
                Transaction transaction = Transaction.builder()
                                .amount(request.amount())
                                .transactionType(TransactionType.ASSET_PURCHASE)
                                .transactionDate(new Date())
                                .sourceAccount(account)
                                .targetAccount(null)
                                .build();

                transactionRepository.save(transaction);

                // Actualizar el balance de la cuenta del usuario
                account.setBalance(account.getBalance().subtract(request.amount()));
                accountRepository.save(account);

                //correo de confirmación
                emailService.sendInvestmentPurchaseEmail(
                                user,                   // Nombre destinatario
                                assetQuantity,          // Cantidad del activo comprada
                                request.assetSymbol(),  // Símbolo del activo
                                request.amount(),       // Monto de la compra
                                asset.getQuantity(),    // Cantidad total del activo
                                asset.getPurchasePrice(), // Precio promedio de compra del activo
                                // assetCurrentPrice,    // Precio actual del activo
                                account.getBalance(),   // Saldo disponible
                                calculateNetWorth()     // Patrimonio netouser.getEmail()
                );
        }

        @Override
        @Transactional
        public void sellAsset(SellAssetRequest request) throws Exception {
                securityService.verifyPin(request.pin());

                BigDecimal assetCurrentPrice = getAssetPrice(request.assetSymbol());

                User user = securityService.getCurrentAuthenticatedUser();

                Account account = securityService.getAccountForCurrentUser();

                Asset asset = assetRepository.findByUserAndAssetSymbol(user, request.assetSymbol())
                                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

                if (asset.getQuantity().compareTo(request.quantity()) < 0) {
                        throw new InsufficientAssetQuantityException("Not enough asset quantity.");
                }

                // 4. Vender el activo: actualizar cantidad del activo
                BigDecimal remainingQuantity = asset.getQuantity().subtract(request.quantity());
                asset.setQuantity(remainingQuantity);
                assetRepository.save(asset);

                // 5. Calcular ganancia o pérdida
                BigDecimal totalSale = request.quantity().multiply(assetCurrentPrice);
                BigDecimal profitOrLoss = totalSale.subtract(request.quantity().multiply(asset.getPurchasePrice()));

                // 6. Actualizar balance de la cuenta con el total de la venta
                BigDecimal newBalance = account.getBalance().add(totalSale);
                account.setBalance(newBalance);
                accountRepository.save(account);

                // 7. Guardar la transacción
                Transaction transaction = Transaction.builder()
                                .amount(totalSale)
                                .transactionType(TransactionType.ASSET_SELL)
                                .transactionDate(new Date())
                                .sourceAccount(account)
                                .targetAccount(null)
                                .build();
                transactionRepository.save(transaction);

                // 8. Enviar correo de confirmación
                emailService.sendInvestmentSaleEmail(
                                user,                           // Usuario
                                request.quantity(),             // Cantidad vendida
                                request.assetSymbol(),          // Símbolo del activo
                                profitOrLoss,                   // Ganancia o pérdida
                                remainingQuantity,              // Cantidad restante del activo
                                asset.getPurchasePrice(),       // Precio promedio de compra del activo
                                newBalance,                     // Balance actualizado de la cuenta
                                calculateNetWorth()             // Patrimonio netouser.getEmail()
                );
        }

        @Override
        public BigDecimal calculateNetWorth() throws Exception {
                User user = securityService.getCurrentAuthenticatedUser();

                BigDecimal netWorth = user.getAccount().stream()
                                .map(Account::getBalance)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<Asset> assets = assetRepository.findByUser(user);
                for (Asset asset : assets) {
                        BigDecimal currentPrice = getAssetPrice(asset.getAssetSymbol());
                        netWorth = netWorth.add(asset.getQuantity().multiply(currentPrice));
                }

                return netWorth;
        }

        @Override
        public Map<String, BigDecimal> getMarketPrices() throws Exception {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map<String, BigDecimal>> response = restTemplate.exchange(marketApiUrl, HttpMethod.GET,
                                null,
                                new ParameterizedTypeReference<Map<String, BigDecimal>>() {
                                });
                return response.getBody();
        }

        @Override
        public BigDecimal getAssetPrice(String assetSymbol) throws Exception {
                Map<String, BigDecimal> marketPrices = getMarketPrices();
                BigDecimal price = marketPrices.get(assetSymbol);
                if (price == null) {
                        throw new ResourceNotFoundException("Price not found for asset symbol: " + assetSymbol);
                }
                return price;
        }


}
