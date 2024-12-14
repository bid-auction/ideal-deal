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

import java.util.UUID;

@PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
@RestController
@RequestMapping("api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final JWTUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> registerProduct(@RequestHeader("Authorization") String token,
                                             @Valid @RequestBody ProductDto.Request request){

        String memberIdStr = jwtUtil.getMemberIdFromToken(token);

        UUID memberId = UUID.fromString(memberIdStr);

        return ResponseEntity.ok(productService.register(memberId, request));
    }
}
