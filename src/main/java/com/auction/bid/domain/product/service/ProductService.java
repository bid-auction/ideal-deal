package com.auction.bid.domain.product.service;

import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.dto.ProductDto;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public interface ProductService {
    ProductDto.Response register(ProductDto.Request request);
}