package com.auction.bid.domain.redisCart;

import com.auction.bid.global.exception.exceptions.CartOperationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CartRepositoryTest {

    @Test
    void addToCart() {
        // given: 모의 객체 생성 및 예외 발생 시뮬레이션
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);

        // RedisTemplate의 opsForHash() 호출 시, 모의 HashOperations 반환
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        // 모의 HashOperations의 get() 호출 시 DataRetrievalFailureException 발생하도록 설정
        when(mockHashOps.get(anyString(), any())).thenThrow(new DataRetrievalFailureException("Simulated Redis Exception"));

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);
        String userId = "user123";
        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .quantity(1)
                .build();

        // when & then: addToCart를 호출하면 CartOperationException이 발생해야함.
        assertThrows(CartOperationException.class, () -> {
            cartRepository.addToCart(userId, cartItem);
        });
    }

    @Test
    void getCart(){
       // GIVEN: 모의 객체 생성 및 초기 설정
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);

        // RedisTemplate의 opsForHash() 호출 시, 모의 HashOperations 반환
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        // addToCart() 호출 시 , 동일 상품이 없다고 가정하여 null 반환
        when(mockHashOps.get(anyString(), any())).thenReturn(null);
        // put() 메소드는 void이므로 별도 설정 없이 진행

        // 특정 키("cart:user123") 조회 시 , 예외 발생을 시뮬레이션
        String key = "cart:user123";
        when(mockHashOps.entries(key))
                .thenThrow(new DataRetrievalFailureException("Simulated Exception"));

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);
        String userId = "user123";
        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .name("desktop")
                .quantity(1)
                .price(1000000)
                .build();

        // WHEN: addToCart() 호출해서 장바구니에 데이터를 추가 (여기서는 모의 객체에 의해 put()이 호출됨)
        cartRepository.addToCart(userId, cartItem);

        // THEN: getCart() 호출 시, 위에서 시뮬레이션한 예외가 발생하여 CartOperationException 이 던져져야 함
        assertThrows(CartOperationException.class, () -> {
            cartRepository.getCart(userId);
        });
    }

    @Test
    void removeFromCart_NoExistingItem() {
       // Given: 존재하지 않는 항목: get() 호출 시 null 반환
        String userId = "user123";
        CartItem cartItem = CartItem.builder()
                .productId("prod123")
                .name("Test Product")
                .quantity(1)
                .price(1000000)
                .build();
        int quantityToRemove = 1;

        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        HashOperations<String, Object, Object> mockHashOps = mock(HashOperations.class);
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOps);
        when(mockHashOps.get("cart:" + userId, cartItem.getProductId())).thenReturn(null);

        CartRepository cartRepository = new CartRepository(mockRedisTemplate);

        // When: removeFromCart를 호출
        cartRepository.removeFromCart(userId, cartItem, quantityToRemove);

        // Then: get()이 null을 반환하였으므로, put()이 delete() 호출이 없어야 함.
        Mockito.verify(mockHashOps, Mockito.never()).put(Mockito.anyString(), Mockito.any(), Mockito.any());
        Mockito.verify(mockHashOps, Mockito.never()).delete(Mockito.anyString(), Mockito.any());

    }

    @Test
    void clearCart() {
        // GIVEN: 사용자 ID 및 모의RedisTemplate 설정
        String userId = "user123";
        String key = "cart:" + userId;
        RedisTemplate<String, Object> mockRedisTmplate = mock(RedisTemplate.class);

        // delete(key) 호출 시 정상 동작하도록(dP: true 반환) 설정
        when(mockRedisTmplate.delete(key)).thenReturn(true);

        CartRepository repository = new CartRepository(mockRedisTmplate);

        // WHEN: clearCart() 호출
        // THEN: 예외 없이 정상적으로 실행되어야 하며, delete 메소드가 정확히 한 번 호출되었음을 검증
        assertDoesNotThrow(()-> repository.clearCart(userId));
        verify(mockRedisTmplate, timeout(1)).delete(key);

    }
}