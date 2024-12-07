package com.auction.bid.domain.product.dto;

import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.product.AuctionPhase;
import com.auction.bid.domain.product.AuctionStatus;
import com.auction.bid.domain.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class ProductDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{

        @NotEmpty(message = "상품 제목은 필수입니다.")
        private String title;

        @NotEmpty(message = "상품 설명은 필수입니다.")
        private String description;

        @NotEmpty(message = "상품 사진은 필수입니다.")
        private List<MultipartFile> filePath;

        @NotEmpty(message = "경매 시작가는 필수입니다.")
        private long startBid;

        @NotNull(message = "경매 시작일은 필수입니다.")
        private LocalDateTime auctionStart;

        @NotNull(message = "상품 종료일은 필수입니다.")
        private LocalDateTime auctionEnd;

        @NotEmpty(message = "카테고리 선택은 필수입니다.")
        private String category;

        public static Product toEntity(Request request){
            Product product =  Product.builder()
                    .title(request.title)
                    .description(request.description)
                    .startBid(request.startBid)
                    .auctionStart(request.auctionStart)
                    .auctionEnd(request.auctionEnd)
                    .build();
            return product;
        }
    }

    @Builder
    @Getter
    public static class Response{
        private String title;
        private String description;
        private Long startBid;
        private LocalDateTime auctionStart;
        private LocalDateTime auctionEnd;

        public static Response fromEntity(Product product){
            return Response.builder()
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .startBid(product.getStartBid())
                    .auctionStart(product.getAuctionStart())
                    .auctionEnd(product.getAuctionEnd())
                    .build();
        }
    }
}
