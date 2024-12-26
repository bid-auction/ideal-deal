package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionHistoryDto {

    private Long auctionId;
    private Long productId;
    private String imageThumbnailUrl;
    private String title;
    private AuctionStatus auctionStatus;
    private Long finalAmount;

    public static AuctionHistoryDto fromAuction(Auction auction) {

        return AuctionHistoryDto.builder()
                .auctionId(auction.getId())
                .productId(auction.getProduct().getId())
                .imageThumbnailUrl(auction.getProduct().getPhotos().get(0).getImagePath())
                .title(auction.getProduct().getTitle())
                .auctionStatus(auction.getAuctionStatus())
                .finalAmount(auction.getAuctionWinnerPrice())
                .build();
    }

}
