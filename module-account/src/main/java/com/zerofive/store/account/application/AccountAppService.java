package com.zerofive.store.account.application;

import com.zerofive.store.account.application.dto.AccountResult;
import com.zerofive.store.account.application.dto.LoginResult;
import com.zerofive.store.account.domain.Account;
import com.zerofive.store.account.domain.AccountService;
import com.zerofive.store.account.infra.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountAppService {

    private final AccountService accountDomainService;
    private final JwtProvider jwtProvider;

    public AccountResult signup(String email, String password, String name) {
        Account account = accountDomainService.createAccount(email, password, name);
        return toResult(account);
    }

    public LoginResult login(String email, String password) {
        Account account = accountDomainService.authenticate(email, password);
        String token = jwtProvider.createToken(account.getId(), account.getEmail(), account.getRole().name());
        return new LoginResult(token);
    }

    public AccountResult getAccount(Long accountId) {
        Account account = accountDomainService.getAccount(accountId);
        return toResult(account);
    }

    private AccountResult toResult(Account account) {
        return new AccountResult(
                account.getId(),
                account.getEmail(),
                account.getName(),
                account.getRole().name()
        );
    }
}
