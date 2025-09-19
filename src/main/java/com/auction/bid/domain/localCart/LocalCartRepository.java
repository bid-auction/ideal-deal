package com.auction.bid.domain.localCart;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LocalCartRepository {

    private final ConcurrentHashMap<String, Map<String, Integer>> storage = new ConcurrentHashMap<>();

    public void addOrUpdate(String userId, String productId, int delta){
        storage
                .computeIfAbsent(userId, k->new HashMap<>())
                .merge(productId, delta, Integer::sum);
    }

    public Map<String, Integer> findAll(String userId){
        return new HashMap<>(storage.getOrDefault(userId, Collections.emptyMap()));
    }
}
