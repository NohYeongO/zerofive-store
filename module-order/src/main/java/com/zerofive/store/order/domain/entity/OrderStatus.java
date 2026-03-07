package com.zerofive.store.order.domain.entity;

public enum OrderStatus {
    PAYMENT_COMPLETED,  // 결제 완료
    PREPARING,          // 상품 준비중
    SHIPPING,           // 배송 시작
    DELIVERED           // 배송 완료
}
