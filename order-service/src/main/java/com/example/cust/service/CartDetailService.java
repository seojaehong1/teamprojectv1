package com.example.cust.service;

import com.example.cust.dto.ProductItemDto;
import com.example.cust.model.CartHeader;
import com.example.cust.model.CartItem;
import com.example.cust.model.CartOption;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartDetailService {

    private final CartItemRepository cartItemRepository;
    private final CartHeaderRepository cartHeaderRepository;

    /**
     * 1. 장바구니에 상품 추가 (기존 로직)
     */
    @Transactional
    public List<CartItem> addItemsToCart(CartHeader cartHeader, List<ProductItemDto> productItems) {
        List<CartItem> newCartItems = new ArrayList<>();

        for (ProductItemDto itemDto : productItems) {
            // CartItem 생성
            CartItem cartItem = CartItem.builder()
                    .menuCode(itemDto.getMenuCode())
                    .menuName(itemDto.getMenuName())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .cartOptions(new ArrayList<>()) // 옵션 리스트 초기화
                    .build();

            // CartOption 생성 및 연결
            if (itemDto.getOptions() != null) {
                List<CartOption> cartOptions = itemDto.getOptions().stream()
                        .map(optionDto -> CartOption.builder()
                                .optionId(optionDto.getOptionId())
                                .optionPrice(optionDto.getOptionPrice())
                                .optionName(optionDto.getOptionName())
                                .build())
                        .collect(Collectors.toList());
                cartItem.getCartOptions().addAll(cartOptions);
            }
            newCartItems.add(cartItem);
        }

        // 부모(Header)에 자식(Items) 추가
        cartHeader.getCartItems().addAll(newCartItems);
        // CascadeType.ALL 설정에 의해 Header 저장 시 Item, Option도 함께 저장됨
        cartHeaderRepository.save(cartHeader);

        return newCartItems;
    }

    /**
     * 2. 장바구니 상품 수량 수정 (새로 추가)
     */
    @Transactional
    public void updateQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 상품이 없습니다. ID: " + cartItemId));

        // Dirty Checking(변경 감지)으로 자동 업데이트
        cartItem.setQuantity(quantity);
        log.info("수량 변경 완료 - ID: {}, 변경수량: {}", cartItemId, quantity);
    }

    /**
     * 3. 장바구니 상품 삭제 (새로 추가)
     */
    @Transactional
    public void deleteItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new IllegalArgumentException("삭제할 상품이 없습니다. ID: " + cartItemId);
        }

        cartItemRepository.deleteById(cartItemId);
        log.info("상품 삭제 완료 - ID: {}", cartItemId);
    }

    /**
     * 고객 ID로 장바구니 조회
     */
    @Transactional(readOnly = true)
    public CartHeader getCartHeaderByCustomerId(String customerId) {
        return cartHeaderRepository.findByCustomerId(customerId).orElse(null);
    }
}