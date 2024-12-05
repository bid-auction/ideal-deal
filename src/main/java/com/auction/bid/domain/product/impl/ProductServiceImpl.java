package com.auction.bid.domain.product.impl;

import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.repository.ProductRepository;
import com.auction.bid.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product register(Product product){
        return productRepository.save(product);
    }
}
