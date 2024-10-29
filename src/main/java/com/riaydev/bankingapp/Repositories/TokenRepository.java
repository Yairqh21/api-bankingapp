package com.riaydev.bankingapp.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.riaydev.bankingapp.Entities.Token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = """
        select t from Token t inner join t.user u
        where u.id = :id and (t.isExpired = false or t.isRevoked = false)
        """)
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);
}
