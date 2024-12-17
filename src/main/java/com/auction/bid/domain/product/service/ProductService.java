package com.auction.bid.domain.product.service;

import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.dto.ProductDto;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto.Response register(List<MultipartFile> images, ProductDto.Request request, String token);
}
