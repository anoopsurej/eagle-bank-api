package com.eaglebank.api.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
