package com.auction.bid.domain.product;

import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.member.Member;
import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private long startBid;

    @Column(nullable = false)
    private LocalDateTime auctionStart;

    @Column(nullable = false)
    private LocalDateTime auctionEnd;

    @Enumerated(EnumType.STRING)
    private ProductBidPhase productBidPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public void changeAuctionPhase(ProductBidPhase productBidPhase) {
        this.productBidPhase = productBidPhase;
    }

}
