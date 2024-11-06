package com.riaydev.bankingapp.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.riaydev.bankingapp.Entities.Otp;


public interface OtpRepository extends JpaRepository<Otp, Long>{
    Optional<Otp> findByUserId(Long id);
}
