package com.auction.bid.domain.product;

import com.auction.bid.domain.product.dto.ProductDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    ProductDto.Response register(List<MultipartFile> images, ProductDto.Request request, String token);

    //ProductDto.Response update(Long id, List<MultipartFile> images, ProductDto.Request request, String token);

    void delete(Long id);

    ProductDto.Response findById(Long productId);
}
