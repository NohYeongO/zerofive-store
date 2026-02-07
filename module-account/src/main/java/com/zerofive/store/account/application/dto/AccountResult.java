package com.zerofive.store.account.application.dto;

public record AccountResult(
        Long id,
        String email,
        String name,
        String role
) {
}
