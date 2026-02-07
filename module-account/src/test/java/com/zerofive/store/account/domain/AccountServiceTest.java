package com.zerofive.store.account.domain;

import com.zerofive.store.account.infra.AccountRepository;
import com.zerofive.store.core.exception.BusinessException;
import com.zerofive.store.core.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "테스터";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        @Test
        @DisplayName("새로운 계정을 생성한다")
        void success() {
            // given
            given(accountRepository.existsByEmail(EMAIL)).willReturn(false);
            given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Account result = accountService.createAccount(EMAIL, PASSWORD, NAME);

            // then
            assertThat(result.getEmail()).isEqualTo(EMAIL);
            assertThat(result.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(result.getName()).isEqualTo(NAME);
            assertThat(result.getRole()).isEqualTo(Role.USER);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일이면 예외를 던진다")
        void duplicateEmail() {
            // given
            given(accountRepository.existsByEmail(EMAIL)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> accountService.createAccount(EMAIL, PASSWORD, NAME))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 사용 중인 이메일");
        }
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("올바른 이메일과 비밀번호로 인증한다")
        void success() {
            // given
            Account account = Account.createUser(EMAIL, ENCODED_PASSWORD, NAME);
            given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);

            // when
            Account result = accountService.authenticate(EMAIL, PASSWORD);

            // then
            assertThat(result.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 예외를 던진다")
        void emailNotFound() {
            // given
            given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.authenticate(EMAIL, PASSWORD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 예외를 던진다")
        void wrongPassword() {
            // given
            Account account = Account.createUser(EMAIL, ENCODED_PASSWORD, NAME);
            given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> accountService.authenticate(EMAIL, "wrongPassword"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }

    @Nested
    @DisplayName("getAccount")
    class GetAccount {

        @Test
        @DisplayName("ID로 계정을 조회한다")
        void success() {
            // given
            Account account = Account.createUser(EMAIL, ENCODED_PASSWORD, NAME);
            given(accountRepository.findById(1L)).willReturn(Optional.of(account));

            // when
            Account result = accountService.getAccount(1L);

            // then
            assertThat(result.getEmail()).isEqualTo(EMAIL);
            assertThat(result.getName()).isEqualTo(NAME);
        }

        @Test
        @DisplayName("존재하지 않는 ID면 예외를 던진다")
        void notFound() {
            // given
            given(accountRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.getAccount(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("계정을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getAccountByEmail")
    class GetAccountByEmail {

        @Test
        @DisplayName("이메일로 계정을 조회한다")
        void success() {
            // given
            Account account = Account.createUser(EMAIL, ENCODED_PASSWORD, NAME);
            given(accountRepository.findByEmail(EMAIL)).willReturn(Optional.of(account));

            // when
            Account result = accountService.getAccountByEmail(EMAIL);

            // then
            assertThat(result.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 예외를 던진다")
        void notFound() {
            // given
            given(accountRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.getAccountByEmail("notfound@example.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("계정을 찾을 수 없습니다");
        }
    }
}
