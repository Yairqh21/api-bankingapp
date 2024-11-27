package com.riaydev.bankingapp.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.riaydev.bankingapp.Entities.Asset;
import com.riaydev.bankingapp.Entities.User;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByUserAndAssetSymbol(User user, String assetSymbol);

    List<Asset> findByUser(User user);

}

