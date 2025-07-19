package com.riaydev.bankingapp.Repositories;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.User;

import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUser(User user);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :accountId AND a.balance >= :amount")
    int debitAccount(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :accountId")
    int creditAccount(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

}
