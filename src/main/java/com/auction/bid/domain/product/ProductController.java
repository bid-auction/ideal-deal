package com.auction.bid.domain.product;

import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.global.security.ConstSecurity;
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

    private final ProductService productService;

    @PostMapping
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public ResponseEntity<?> registerProduct(
            @Valid @RequestParam("images") List<MultipartFile> images,
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
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    public void deleteProduct(@PathVariable(name = "productId") Long id){
        productService.delete(id);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getMainPage() {
        return ResponseEntity.ok(productService.getMainPage());
    }

    @GetMapping("/bid/before")
    public ResponseEntity<?> getBifBefore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getBidBefore(page, size));
    }

    @GetMapping("/bid/ongoing")
    public ResponseEntity<?> getBidOngoing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getBidOngoing(page, size));
    }

    @GetMapping("/bid/ended")
    public ResponseEntity<?> getBidEnded(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getBidEnded(page, size));
    }

    @GetMapping("/sale/rankings")
    public ResponseEntity<?> getRankings() {
        return ResponseEntity.ok(productService.getRankings());
    }

    @GetMapping("/sale/rankings/highest")
    public ResponseEntity<?> getRankingsHighest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getRankingsHighest(page, size));
    }

    @GetMapping("/sale/rankings/lowest")
    public ResponseEntity<?> getRankingsLowest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getRankingsLowest(page, size));
    }

    @GetMapping("/hot")
    public ResponseEntity<?> getHotPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getHotPage(page, size));
    }

}
