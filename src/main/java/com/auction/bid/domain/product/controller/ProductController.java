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
                                                @RequestHeader("Authorization") String token){

        String memberIdStr = jwtUtil.getMemberIdFromToken(token);

        UUID memberId = UUID.fromString(memberIdStr);

        return ResponseEntity.ok(productService.register(images, request, memberId));
    }
}
