package com.auction.bid.domain.redisCart;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface CartService {

   void addToCart(String userId, CartItem cartItem);

   Map<Object, Object> getCart(String userId);

   void removeFromCart(String userId, CartItem item, int quantityToRemove);

   void clearCart(String userId);
}
