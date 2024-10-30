package com.riaydev.bankingapp.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.riaydev.bankingapp.Entities.Account;
import com.riaydev.bankingapp.Entities.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);

}
