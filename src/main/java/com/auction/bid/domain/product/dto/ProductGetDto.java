package com.auction.bid.domain.product.dto;

import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.Product;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProductGetDto {
    @Builder
    @Getter
    public static class Response{
        private Long id;
        private String title;
        private String description;
        private List<String> imagePath;
        private Long startBid;
        private LocalDateTime auctionStart;
        private LocalDateTime auctionEnd;
        private ProductBidPhase productBidPhase;
        private String memberName;
        private String categoryName;

        public static Response fromEntity(Product product, List<Photo> photos){
            return Response.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .imagePath(photos.stream().map(Photo::getImagePath).collect(Collectors.toList()))
                    .startBid(product.getStartBid())
                    .auctionStart(product.getAuctionStart())
                    .auctionEnd(product.getAuctionEnd())
                    .productBidPhase(product.getProductBidPhase())
                    .memberName(product.getMember().getName())
                    .categoryName(product.getCategory().getCategoryName())
                    .build();
        }
    }
}