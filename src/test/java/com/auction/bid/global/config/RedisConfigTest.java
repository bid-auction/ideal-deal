package com.auction.bid.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    @Qualifier("cartRedisTemplate")
    private RedisTemplate<String, Object> cartRedisTemplate;

    @Test
    void cartRedisTemplate_setAndGet() {
        String testKey = "test:cart";
        String testValue = "Hello, Redis!";

        //given
        cartRedisTemplate.opsForValue().set(testKey, testValue);

        //when
        String value = (String) cartRedisTemplate.opsForValue().get(testKey);

        //then
        assertEquals(testValue, value, "Redis에 저장한 데이터와 조회한 데이터가 같아야 합니다.");
    }
}