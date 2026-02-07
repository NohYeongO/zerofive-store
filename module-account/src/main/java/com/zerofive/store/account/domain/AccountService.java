package com.zerofive.store.account.domain;

import com.zerofive.store.account.infra.AccountRepository;
import com.zerofive.store.core.exception.BusinessException;
import com.zerofive.store.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account createAccount(String email, String password, String name) {
        if (accountRepository.existsByEmail(email)) {
            throw new BusinessException(400, "이미 사용 중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);
        Account account = Account.createUser(email, encodedPassword, name);
        return accountRepository.save(account);
    }

    public Account authenticate(String email, String password) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(401, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BusinessException(401, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return account;
    }

    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("계정을 찾을 수 없습니다."));
    }

    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("계정을 찾을 수 없습니다."));
    }
}
