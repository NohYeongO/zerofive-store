package com.zerofive.store.account.application;

import com.zerofive.store.account.domain.Account;
import com.zerofive.store.account.domain.AccountService;
import com.zerofive.store.core.port.AccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountPortService implements AccountPort {

    private final AccountService accountDomainService;

    @Override
    public AccountInfo getAccount(Long accountId) {
        Account account = accountDomainService.getAccount(accountId);
        return new AccountInfo(account.getId(), account.getEmail(), account.getName());
    }
}
