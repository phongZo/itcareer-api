package com.base.auth.repository;

import com.base.auth.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Account findAccountByUsername(String username);
    Account findAccountByEmail(String email);
    Account findAccountByPhone(String phone);
    Account findAccountByResetPwdCode(String resetPwdCode);
    Account findAccountByEmailOrUsername(String email, String username);
    Page<Account> findAllByKind(int kind, Pageable pageable);
}
