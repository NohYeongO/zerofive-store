package com.zerofive.store.core.port;

public interface AccountPort {

    AccountInfo getAccount(Long accountId);

    record AccountInfo(Long accountId, String email, String name) {
    }
}
