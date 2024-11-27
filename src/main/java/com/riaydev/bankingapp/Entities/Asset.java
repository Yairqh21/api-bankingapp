package com.riaydev.bankingapp.Entities;


import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_symbol")
    private String assetSymbol;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePrice; 

   @Column(nullable = false)
   private BigDecimal quantity; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 
}