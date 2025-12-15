package com.example.cust.service;

import com.example.cust.dto.ProductItemDto;
import com.example.cust.model.CartHeader;
import com.example.cust.model.CartItem;
import com.example.cust.model.CartOption;
import com.example.cust.repository.CartHeaderRepository;
import com.example.cust.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartDetailService {

    private final CartItemRepository cartItemRepository;
    private final CartHeaderRepository cartHeaderRepository;

    /**
     * [주요 API] 여러 ProductItemDto를 CartItem 및 CartOption 엔티티로 변환하여 DB에 저장합니다.
     * 💡 NOT NULL 필드인 menuName 및 optionName 필드에 값을 설정하도록 수정되었습니다.
     * @param cartHeader 현재 장바구니 헤더 (CartItem과 연결됨)
     * @param productItems 장바구니에 추가할 상품/옵션 리스트
     * @return 저장된 CartItem 엔티티 리스트
     */
    @Transactional
    public List<CartItem> addItemsToCart(CartHeader cartHeader, List<ProductItemDto> productItems) {

        List<CartItem> newCartItems = new ArrayList<>();

        for (ProductItemDto itemDto : productItems) {

            // 1. ProductItemDto를 CartItem 엔티티로 변환
            CartItem cartItem = CartItem.builder()
                    .cartHeader(cartHeader) // 연관된 CartHeader 설정
                    .menuCode(itemDto.getMenuCode())
                    // 💡 [수정] menuName 설정 추가
                    .menuName(itemDto.getMenuName())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();

            // 2. ProductItemDto 내의 옵션 리스트를 CartOption 엔티티로 변환
            List<CartOption> cartOptions = itemDto.getOptions().stream()
                    .map(optionDto -> CartOption.builder()
                            .optionId(optionDto.getOptionId())
                            .optionPrice(optionDto.getOptionPrice())
                            // 💡 [수정] optionName 설정 추가
                            .optionName(optionDto.getOptionName())
                            .build())
                    .collect(Collectors.toList());

            // 3. CartItem에 CartOption 리스트 연결 및 양방향 관계 설정
            cartItem.setCartOptions(cartOptions);

            newCartItems.add(cartItem);
        }

        // 4. CartItemRepository를 통해 DB에 저장 (이 라인이 오류 로그의 62번째 라인일 가능성이 높습니다.)
        return cartItemRepository.saveAll(newCartItems);
    }

    @Transactional(readOnly = true) // 단순 조회이므로 readOnly = true 권장
    public CartHeader getCartHeaderByCustomerId(Integer customerId) {
        // 실제 구현에서는 상태(예: ACTIVE) 등을 확인하여 조회해야 할 수 있지만,
        // 현재는 ID로만 조회한다고 가정합니다.
        return cartHeaderRepository.findByCustomerId(customerId)
                .orElse(null); // Optional 대신 null을 반환하도록 처리 (또는 Optional 반환)
    }
}