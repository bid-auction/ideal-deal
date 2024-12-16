package com.auction.bid.domain.product.controller;

import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.domain.product.service.ProductService;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JWTUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> registerProduct(   @Valid @RequestParam("images") List<MultipartFile> images,
                                                @ModelAttribute ProductDto.Request request,
                                                @RequestHeader(ConstSecurity.AUTHORIZATION) String token){

        if (productService == null){
            throw new RuntimeException("ProductService 빈이 주입되지 않았습니다.");
        }

        System.out.println("Received Token: " + token);

        System.out.println("이미지 개수: " + images.size());

        System.out.println("Request DTO: " + request);

        String memberIdStr=null;

        try {
            memberIdStr = jwtUtil.getMemberIdFromToken(token);
            System.out.println("Extracted Member ID (String): " + memberIdStr);
        }catch (Exception e){
            System.out.println("Exception during token parsing: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        if(memberIdStr == null || memberIdStr.isBlank()){
            throw new RuntimeException("Failed to extract memberId from token.");
        }

        try {
            UUID memberId = UUID.fromString(memberIdStr);
            System.out.println("Parsed UUID: " + memberId);

            return ResponseEntity.ok(productService.register(images, request, memberId));
        }catch (IllegalArgumentException e){
                throw new RuntimeException("Invalid UUID format: " + memberIdStr, e);
        }

    }
}
