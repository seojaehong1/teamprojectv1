package com.example.cust.service;

import com.example.cust.model.CartHeader;
import com.example.cust.repository.CartHeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MakeCart {

    private final CartHeaderRepository cartHeaderRepository;

    @Transactional
    public CartHeader getOrCreateCartHeader(int customerId) {

        // 1. 고객 ID를 기준으로 기존 CartHeader를 조회합니다.
        Optional<CartHeader> existingHeader = cartHeaderRepository.findByCustomerId(customerId);

        // 2. 기존 헤더가 존재하면 그것을 반환합니다. (중복 생성 방지)
        if (existingHeader.isPresent()) {
            System.out.println("[CartService] 기존 CartHeader 사용. ID: " + existingHeader.get().getCartId());
            return existingHeader.orElse(null);
        }

        // 3. 기존 헤더가 없으면 새로 생성합니다. (요청하신 날짜 정보 포함)
        CartHeader newHeader = CartHeader.builder()
                .customerId(customerId)
                // 현재 시간을 created_at에 저장합니다.
                .createdAt(LocalDateTime.now())
                .build();

        // 4. 데이터베이스에 저장합니다.
        CartHeader savedHeader = cartHeaderRepository.save(newHeader);

        System.out.println("[CartService] 새로운 CartHeader 생성 완료. ID: " + savedHeader.getCartId());

        return savedHeader;
    }
}