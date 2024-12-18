package com.auction.bid.domain.bid;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BidDto {

    private Long productId;
    private Long memberId;
    private String nickname;
    private Long bidAmount;
    private LocalDateTime bidTime;

    public static Bid toBidEntity(BidDto bidDto, Member member, Product product) {

        return Bid.builder()
                .member(member)
                .product(product)
                .bidAmount(bidDto.getBidAmount())
                .bidTime(bidDto.getBidTime())
                .build();
    }

}
