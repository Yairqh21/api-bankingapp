package com.riaydev.bankingapp.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.User;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUser(User user);
}

