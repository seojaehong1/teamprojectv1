package com.example.cust.model;

public enum OrderStatus {
    /** 주문 및 결제가 완료된 상태 (초기 상태) */
    PAYMENT_COMPLETED("결제 완료"),

    /** 상품 제작 또는 준비 중인 상태 */
    IN_PRODUCTION("제작 중"),

    /** 상품이 고객에게 전달될 준비가 완료된 상태 */
    COMPLETED("완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}