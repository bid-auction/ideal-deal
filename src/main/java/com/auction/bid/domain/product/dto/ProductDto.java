package com.auction.bid.domain.product.dto;

import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.Product;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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

        @NotEmpty(message = "경매 시작가는 필수입니다.")
        private long startBid;

        @NotNull(message = "경매 시작일은 필수입니다.")
        private LocalDateTime auctionStart;

        @NotNull(message = "상품 종료일은 필수입니다.")
        private LocalDateTime auctionEnd;

        @NotEmpty(message = "카테고리 선택은 필수입니다.")
        private String category;

        public static Product toEntity(Request request, Member member, Category category){
            return Product.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .startBid(request.getStartBid())
                    .auctionStart(request.getAuctionStart())
                    .auctionEnd(request.getAuctionEnd())
                    .member(member)
                    .category(category)
                    .productBidPhase(ProductBidPhase.BEFORE)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class Response{
        private Long id;
        private String title;
        private String description;
        private Long startBid;
        private LocalDateTime auctionStart;
        private LocalDateTime auctionEnd;

        public static Response fromEntity(Product product){
            return Response.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .startBid(product.getStartBid())
                    .auctionStart(product.getAuctionStart())
                    .auctionEnd(product.getAuctionEnd())
                    .build();
        }
    }
}
