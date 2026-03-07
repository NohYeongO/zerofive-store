package com.zerofive.store.payment.application;

import com.zerofive.store.core.port.PaymentPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class PaymentPortService implements PaymentPort {

    private static final double SUCCESS_RATE = 0.8;
    private final Random random = new Random();

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("결제 요청: accountId={}, amount={}, orderId={}",
                request.accountId(), request.amount(), request.orderId());

        boolean success = random.nextDouble() < SUCCESS_RATE;

        if (success) {
            String transactionId = UUID.randomUUID().toString();
            log.info("결제 성공: transactionId={}", transactionId);
            return new PaymentResult(true, transactionId, null);
        } else {
            String failReason = "결제 승인 거부 (잔액 부족 또는 카드사 오류)";
            log.warn("결제 실패: reason={}", failReason);
            return new PaymentResult(false, null, failReason);
        }
    }
}
