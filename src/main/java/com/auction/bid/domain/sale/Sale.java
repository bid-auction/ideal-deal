package com.auction.bid.domain.sale;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.Product;
import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Sale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Long id;

    private Long salePrice;

    @Enumerated(EnumType.STRING)
    private SaleStatus saleStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public static Sale fromAuction(Long finalPrice, SaleStatus saleStatus, Member member, Product product) {
        return Sale.builder()
                .salePrice(finalPrice)
                .saleStatus(saleStatus)
                .member(member)
                .product(product)
                .build();
    }

}
