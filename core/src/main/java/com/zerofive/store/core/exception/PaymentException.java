package com.zerofive.store.core.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final String errorCode;

    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static PaymentException failed(String reason) {
        return new PaymentException("PAYMENT_FAILED", reason);
    }
}
