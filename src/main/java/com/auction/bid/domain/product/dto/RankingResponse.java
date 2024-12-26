package com.auction.bid.domain.product.dto;


import com.auction.bid.domain.sale.Sale;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingResponse {

    private Long productId;
    private String imageThumbnailUrl;
    private String title;
    private Long finalAmount;

    public static Map<String, List<RankingResponse>> fromSaleListToMap(Map<String, List<Sale>> rankingForThisWeek) {
        List<Sale> hL = rankingForThisWeek.get("HighestList");
        List<Sale> lL = rankingForThisWeek.get("LowestList");

        List<RankingResponse> highestList = hL.stream()
                .map(RankingResponse::fromSaleEntity)
                .toList();

        List<RankingResponse> lowestList = lL.stream()
                .map(RankingResponse::fromSaleEntity)
                .toList();

        Map<String, List<RankingResponse>> res = new HashMap<>();
        res.put("HighestList", highestList);
        res.put("LowestList", lowestList);

        return res;
    }

    public static Page<RankingResponse> fromSalePageToResPage(Page<Sale> salePage) {
        List<RankingResponse> list = salePage.stream()
                .map(RankingResponse::fromSaleEntity)
                .toList();

        return PageableExecutionUtils.getPage(list, salePage.getPageable(), salePage::getTotalElements);
    }

    private static RankingResponse fromSaleEntity(Sale sale) {
        return RankingResponse.builder()
                .productId(sale.getProduct().getId())
                .imageThumbnailUrl(sale.getProduct().getPhotos().get(0).getImagePath())
                .title(sale.getProduct().getTitle())
                .finalAmount(sale.getSalePrice())
                .build();
    }

}
