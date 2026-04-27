package com.eaglebank.api.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    private String accountNumber;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(nullable = false)
    private Instant updatedTimestamp;
}
