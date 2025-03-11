package com.auction.bid.domain.redisCart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Repository
public class CartRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public CartRepository(@Qualifier("cartRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 사용자별 장바구니의 Redis key 생성(예:"cart:user123")
    private String getKey(String userId){
        return "cart:" + userId;
    }

    // 장바구니 항목 추가 또는 업데이트
    // 동일 productId가 있으면 기존 cartItem의 수량을 증가
    public void addToCart(String userId, CartItem cartItem){
        String key = getKey(userId);
        // 이미 같은 상품이 존재하는지 확인
        Object existingObj = redisTemplate.opsForHash().get(key, cartItem.getProductId());
        if (existingObj != null){
            CartItem existingItem = (CartItem) existingObj;
            // 수량을 누적해서 증가
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            redisTemplate.opsForHash().put(key, cartItem.getProductId(), existingItem);
        }else {
            // 없으면 새 CartItem 추가
            redisTemplate.opsForHash().put(key, cartItem.getProductId(), cartItem);
        }
    }

    // 장바구니 목록 조회
    public Map<Object, Object> getCart(String userId){
        return redisTemplate.opsForHash().entries("cart:"+userId);
    }

    // 장바구니 항목 삭제
    public void removeFromCart(String userId, CartItem item, int quantityToRemove){

        String key = "cart:" + userId;

        // RedisTemplate의 key, hashKey serializer 가져오기
        RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();

        // 키와 필드를 직렬화 (바이트 배열로 변환)
        byte[] serializedKeyBytes = keySerializer.serialize(key);
        byte[] serializedFieldBytes = hashKeySerializer.serialize(item.getProductId());

        // 바이트 배열을 UTF-8 문자열로 변환하여 로그 출력
        String serializedKey = new String(serializedKeyBytes, StandardCharsets.UTF_8);
        String serializedField = new String(serializedFieldBytes, StandardCharsets.UTF_8);

        System.out.println("Serialized Key: " + serializedKey);
        System.out.println("Serialized Field: " + serializedField);

        CartItem currentItem = (CartItem) redisTemplate.opsForHash().get(key, item.getProductId());
        if (currentItem != null){
            int newQuantity = currentItem.getQuantity()-quantityToRemove;
            if (newQuantity > 0){
                currentItem.setQuantity(newQuantity);
                redisTemplate.opsForHash().put(key, item.getProductId(), currentItem);
            }else {
                Long removedCount = redisTemplate.opsForHash().delete(key, item.getProductId());
                System.out.println("Deleted count: " + removedCount);
            }
        }



    }

    // 장바구니 비우기
    public void clearCart(String userId){
        redisTemplate.delete("cart:" + userId);
    }
}
