package com.auction.bid.domain.localCart;

import com.auction.bid.domain.redisCart.CartItem;
import com.auction.bid.domain.redisCart.CartService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("localCartService")
public class LocalCartServiceImpl implements CartService {
    private final LocalCartRepository localCartRepository;

    public LocalCartServiceImpl(LocalCartRepository localCartRepository) {
        this.localCartRepository = localCartRepository;
    }

    @Override
    public void addToCart(String userId, CartItem cartItem) {
        localCartRepository.addOrUpdate(userId, cartItem.getProductId(), cartItem.getQuantity());
    }

    @Override
    public List<CartItem> getCart(String userId){
        // {prodId -> qty }를 List<CartItem> 으로 변환
        return localCartRepository.findAll(userId).entrySet().stream()
                .map(e -> {
                    CartItem i = new CartItem();
                    i.setProductId(e.getKey());
                    i.setQuantity(e.getValue());
                    return i;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromCart(String userId, CartItem item, int quantityToRemove) {

    }

    @Override
    public void clearCart(String userId) {

    }
}
