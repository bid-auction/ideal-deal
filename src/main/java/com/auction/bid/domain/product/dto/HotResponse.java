package com.auction.bid.domain.product.dto;

import com.auction.bid.domain.product.Product;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HotResponse {

    private Long productId;
    private String imageThumbnailUrl;
    private String title;

    public static HotResponse fromProductEntity(Product product) {
        return HotResponse.builder()
                .productId(product.getId())
                .imageThumbnailUrl(product.getPhotos().get(0).getImagePath())
                .title(product.getTitle())
                .build();
    }

}
