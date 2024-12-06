package com.auction.bid.domain.product.impl;

import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.domain.product.repository.ProductRepository;
import com.auction.bid.domain.product.service.ProductService;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.InvalidAuctionEndTimeStartAfterException;
import com.auction.bid.global.exception.exceptions.InvalidAuctionStartTimeNowAfterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductDto.Response register(ProductDto.Request request){

        if(request.getAuctionStart().isBefore(LocalDateTime.now())){
            throw new InvalidAuctionStartTimeNowAfterException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
        }
        if(request.getAuctionEnd().isBefore(request.getAuctionStart())){
            throw new InvalidAuctionEndTimeStartAfterException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
        }

        Product product =  ProductDto.Request.toEntity(request);
        return ProductDto.Response.fromEntity(productRepository.save(product));
    }
}
