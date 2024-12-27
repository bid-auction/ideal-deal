package com.auction.bid.domain.product.dto;

import com.auction.bid.domain.product.Product;
import lombok.*;

@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PhaseCriteriaResponse {

    private Long productId;
    private String imageThumbnailUrl;
    private String title;

    public static PhaseCriteriaResponse fromProductEntity(Product product) {
        return PhaseCriteriaResponse.builder()
                .productId(product.getId())
                .imageThumbnailUrl(product.getPhotos().get(0).getImagePath())
                .title(product.getTitle())
                .build();
    }

}
