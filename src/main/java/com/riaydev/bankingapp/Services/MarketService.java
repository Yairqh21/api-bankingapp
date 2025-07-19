package com.riaydev.bankingapp.Services;

import java.math.BigDecimal;

import com.riaydev.bankingapp.DTO.BuyAssetRequest;
import com.riaydev.bankingapp.DTO.SellAssetRequest;


public interface MarketService {

    public void buyAsset(BuyAssetRequest request) throws Exception ;

    public void sellAsset(SellAssetRequest request) throws Exception;

    public BigDecimal calculateNetWorth()throws Exception;

}
