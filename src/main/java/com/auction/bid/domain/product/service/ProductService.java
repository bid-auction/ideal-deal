package com.auction.bid.domain.product.service;

import com.auction.bid.domain.product.Product;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public interface ProductService {
    Product register(Product product);
}
