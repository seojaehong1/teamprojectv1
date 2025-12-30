package com.example.cust.service;

import com.example.cust.dto.ProductItemDto;
import com.example.cust.model.CartHeader;
import com.example.cust.model.CartItem;
import com.example.cust.model.CartOption;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service; // 1. Bean 등록을 위해 반드시 필요
import org.springframework.transaction.annotation.Transactional; // 2. readOnly 옵션을 위해 스프링용 임포트 필요

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartDetailService {

    private final CartItemRepository cartItemRepository;
    private final CartHeaderRepository cartHeaderRepository;

    @Transactional
    public List<CartItem> addItemsToCart(CartHeader cartHeader, List<ProductItemDto> productItems) {

        List<CartItem> newCartItems = new ArrayList<>();

        for (ProductItemDto itemDto : productItems) {

            // 1. CartItem 생성 (cartHeader 참조 제거)
            CartItem cartItem = CartItem.builder()
                    // .cartHeader(cartHeader) <- [삭제] 단방향이므로 이 필드는 엔티티에 없어야 함
                    .menuCode(itemDto.getMenuCode())
                    .menuName(itemDto.getMenuName())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();

            // 2. CartOption 생성 (cartItem 참조 없이 생성)
            if (itemDto.getOptions() != null) {
                List<CartOption> cartOptions = itemDto.getOptions().stream()
                        .map(optionDto -> CartOption.builder()
                                .optionId(optionDto.getOptionId())
                                .optionPrice(optionDto.getOptionPrice())
                                .optionName(optionDto.getOptionName())
                                // .cartItem(cartItem) <- [삭제] 단방향
                                .build())
                        .collect(Collectors.toList());

                // 3. CartItem의 리스트에 옵션들 추가
                cartItem.getCartOptions().addAll(cartOptions);
            }

            newCartItems.add(cartItem);
        }

        // 4. 부모인 CartHeader의 리스트에 새로운 아이템들을 추가
        // 단방향 @OneToMany + @JoinColumn 설정 덕분에 이렇게만 해도 외래키가 저장됩니다.
        cartHeader.getCartItems().addAll(newCartItems);

        // 5. 부모를 저장하면 Cascade에 의해 자식들도 함께 저장/수정됩니다.
        cartHeaderRepository.save(cartHeader);

        return newCartItems;
    }

    @Transactional(readOnly = true)
    public CartHeader getCartHeaderByCustomerId(String customerId) {
        return cartHeaderRepository.findByCustomerId(customerId).orElse(null);
    }
}