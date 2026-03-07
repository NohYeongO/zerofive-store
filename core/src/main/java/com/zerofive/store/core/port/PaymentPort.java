package com.zerofive.store.core.port;

public interface PaymentPort {

    PaymentResult processPayment(PaymentRequest request);

    record PaymentRequest(Long accountId, int amount, String orderId) {
    }

    record PaymentResult(boolean success, String transactionId, String failReason) {
    }
}
