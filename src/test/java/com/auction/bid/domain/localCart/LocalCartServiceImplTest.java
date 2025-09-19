package com.auction.bid.domain.localCart;

import com.auction.bid.domain.redisCart.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalCartServiceImplTest {

    LocalCartRepository mockRepository;
    LocalCartServiceImpl localCartServiceImpl;
    final String userId = "user123";

    @BeforeEach
    void setUp(){
        // Given: 매 테스트마다 깨끗한 mock 레포지토리와 서비스 인스턴스 준비
        mockRepository = mock(LocalCartRepository.class);
        localCartServiceImpl = new LocalCartServiceImpl(mockRepository);
    }

    @Test
    void getCart_emptyCart_returnsEmptyList() {
        // Given: 레포지토리가 빈 맵을 리턴하도록 설정
        when(mockRepository.findAll(userId))
                .thenReturn(Collections.emptyMap());

        // When: 서비스의 getCart를 호출하면
        List<CartItem> result = localCartServiceImpl.getCart(userId);

        // Then: null 이 아니고, 빈 리스트여야 한다
        assertNotNull(result, "getCart() should never return null");
        assertTrue(result.isEmpty(), "empty repository -> empty cart list");

        // And: 레포지토리의 findAll(userId) 호출이 1번 일어났음을 검증
        verify(mockRepository, times(1)).findAll(userId);
    }

    @Test
    void getCart_withItems_returnsCorrectCartItems(){
        // Given: 레포지토리가 두 개의(productId+quantity) 맵을 리턴하도록 설정
        Map<String, Integer> fakeMap = Map.of(
                "prodA", 2,
                "prodB", 5
        );
        when(mockRepository.findAll(userId))
                .thenReturn(fakeMap);

        // When: 서비스의 getCart를 호출하면
        List<CartItem> result = localCartServiceImpl.getCart(userId);

        // Then: 리스트 크기와 각 CartItem의 값이 정확해야 한다.
        assertEquals(2, result.size(), "should return two items");

        // Map(productId+quantity) 로 바궈서 검증
        Map<String, Integer> quantities = result.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

        assertEquals(2, quantities.get("prodA"));
        assertEquals(5, quantities.get("prodB"));

        // And: 역시 findAll 호출이 1회
        verify(mockRepository, times(1)).findAll(userId);
    }

    @Test
    void getCart_whenRepositoryThrows_thenPropagatesException(){
        // Given: findAll() 호출 시 런타임 예외를 던지도록 설정
        when(mockRepository.findAll(userId))
                .thenThrow(new IllegalStateException("DB is down"));

        // When & Then: 서비스의 getCart() 호출 시 동일한 예외가 터지는지 확인
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> localCartServiceImpl.getCart(userId)
        );
        assertEquals("DB is down", ex.getMessage());

        // 그리고 findAll()이 1회 호출됐음을 검증
        verify(mockRepository, times(1)).findAll(userId);
    }
}