package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SaleHistoryDto {

    private Long saleId;
    private Long productId;
    private String imageThumbnailUrl;
    private String title;
    private SaleStatus saleStatus;
    private Long finalAmount;

    public static SaleHistoryDto fromSale(Sale sale) {
        return SaleHistoryDto.builder()
                .saleId(sale.getId())
                .productId(sale.getProduct().getId())
                .imageThumbnailUrl(sale.getProduct().getPhotos().get(0).getImagePath())
                .title(sale.getProduct().getTitle())
                .saleStatus(sale.getSaleStatus())
                .finalAmount(sale.getSalePrice())
                .build();
    }

}
