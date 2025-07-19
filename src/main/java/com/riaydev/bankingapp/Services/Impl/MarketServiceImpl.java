package com.riaydev.bankingapp.Services.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.DTO.BuyAssetRequest;
import com.riaydev.bankingapp.DTO.SellAssetRequest;
import com.riaydev.bankingapp.Entities.*;
import com.riaydev.bankingapp.Entities.Transaction.TransactionType;
import com.riaydev.bankingapp.Exceptions.*;
import com.riaydev.bankingapp.Repositories.*;
import com.riaydev.bankingapp.Services.EmailService;
import com.riaydev.bankingapp.Services.MarketService;
import com.riaydev.bankingapp.Services.SecurityService;
import com.riaydev.bankingapp.Services.client.MarketPriceClient;
import com.riaydev.bankingapp.Services.helper.TransactionHelper;

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
        private TransactionHelper transactionHelper;
        @Autowired
        private MarketPriceClient marketPriceClient;

        @Override
        @Transactional
        public void buyAsset(BuyAssetRequest request) throws InsufficientFundsException {

                securityService.verifyPin(request.pin());

                BigDecimal assetCurrentPrice = marketPriceClient.getAssetPrice(request.assetSymbol());

                User user = securityService.getCurrentAuthenticatedUser();

                Account account = securityService.getAccountForCurrentUser();

                securityService.validateSufficientBalance(account, request.amount());

                // Comprar el activo
                BigDecimal assetQuantity = request.amount().divide(assetCurrentPrice, 8, RoundingMode.HALF_UP);
                Asset asset = assetRepository.findByUserAndAssetSymbol(user, request.assetSymbol()).orElse(null);

                if (asset == null) {
                        // Primera compra del activo: establecer precio de compra y cantidad
                        asset = Asset.builder()
                                        .user(user)
                                        .assetSymbol(request.assetSymbol())
                                        .purchasePrice(assetCurrentPrice) // Precio de la primera compra
                                        .quantity(assetQuantity)
                                        .build();
                } else {
                        // Calcular precio promedio ponderado para el activo existente
                        BigDecimal totalInvestment = asset.getPurchasePrice().multiply(asset.getQuantity())
                                        .add(request.amount());
                        BigDecimal totalQuantity = asset.getQuantity().add(assetQuantity);
                        BigDecimal avgPriceAsset = totalInvestment.divide(totalQuantity, 2, RoundingMode.HALF_UP);

                        // Actualizar el activo con la nueva cantidad y precio promedio
                        asset.setQuantity(totalQuantity);
                        asset.setPurchasePrice(avgPriceAsset); // Precio promedio actualizado
                }

                assetRepository.save(asset);

                // Guardar el historial de transacción
                transactionHelper.createTransaction(account, request.amount(), TransactionType.ASSET_PURCHASE);

                // Actualizar el balance de la cuenta del usuario
                account.setBalance(account.getBalance().subtract(request.amount()));
                accountRepository.save(account);

                // Enviar correo de confirmación
                emailService.sendInvestmentPurchaseEmail(
                                user,                   // Nombre destinatario
                                assetQuantity,          // Cantidad del activo comprada
                                request.assetSymbol(),  // Símbolo del activo
                                request.amount(),       // Monto de la compra
                                asset.getQuantity(),    // Cantidad total del activo
                                asset.getPurchasePrice(), // Precio promedio de compra del activo
                                // assetCurrentPrice,   // Precio actual del activo
                                account.getBalance(),   // Saldo disponible
                                calculateNetWorth()     // Patrimonio netouser.getEmail()
                );
        }

        @Override
        @Transactional
        public void sellAsset(SellAssetRequest request)
                        throws InsufficientFundsException, InsufficientAssetQuantityException {
                securityService.verifyPin(request.pin());

                BigDecimal assetCurrentPrice = marketPriceClient.getAssetPrice(request.assetSymbol());

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

                transactionHelper.createTransaction(account, totalSale, TransactionType.ASSET_SELL);

                // 8. Enviar correo de confirmación
                emailService.sendInvestmentSaleEmail(
                                user,                                   // Usuario
                                request.quantity(),                     // Cantidad vendida
                                request.assetSymbol(),                  // Símbolo del activo
                                profitOrLoss.setScale(2, RoundingMode.HALF_UP), // Ganancia o pérdida
                                remainingQuantity,                      // Cantidad restante del activo
                                asset.getPurchasePrice(),               // Precio promedio de compra del activo
                                newBalance.setScale(2, RoundingMode.HALF_UP), // Balance actualizado de la cuenta
                                calculateNetWorth().setScale(2, RoundingMode.HALF_UP) // Patrimonio neto
                );
        }

        @Override
        public BigDecimal calculateNetWorth() throws InsufficientFundsException {
                User user = securityService.getCurrentAuthenticatedUser();

                BigDecimal netWorth = user.getAccount().stream()
                                .map(Account::getBalance)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<Asset> assets = assetRepository.findByUser(user);
                for (Asset asset : assets) {
                        BigDecimal currentPrice = marketPriceClient.getAssetPrice(asset.getAssetSymbol());
                        netWorth = netWorth.add(asset.getQuantity().multiply(currentPrice));
                }

                return netWorth;
        }

}
