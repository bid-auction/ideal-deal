package com.auction.bid.domain.product;

import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
//
    private final ProductService productService;

    @PostMapping
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public ResponseEntity<?> registerProduct(   @Valid @RequestParam("images") List<MultipartFile> images,
                                                @ModelAttribute ProductDto.Request request,
                                                @RequestHeader(ConstSecurity.AUTHORIZATION) String token){

            return ResponseEntity.ok(productService.register(images, request, token));
    }

    @PutMapping("/{productId}")
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public ResponseEntity<?> updateProduct(@PathVariable(name = "productId") Long productId,
                                           @Valid @RequestParam("images") List<MultipartFile> images,
                                           @ModelAttribute ProductDto.Request request,
                                           @RequestHeader(ConstSecurity.AUTHORIZATION) String token){

        return ResponseEntity.ok(productService.update(productId, images, request, token));
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable(name = "productId") Long productId){
        productService.delete(productId);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
