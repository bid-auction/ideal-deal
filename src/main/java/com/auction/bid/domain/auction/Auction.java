package com.auction.bid.domain.auction;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.Product;
import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    private Long auctionWinnerPrice;

    @Enumerated(EnumType.STRING)
    private AuctionStatus auctionStatus;

    private LocalDateTime auction_start;

    private LocalDateTime auction_end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public static Auction fromBid(Member member, Product product, Long finalAmount, AuctionStatus auctionStatus) {
        return Auction.builder()
                .member(member)
                .product(product)
                .auctionWinnerPrice(finalAmount)
                .auctionStatus(auctionStatus)
                .build();
    }

}