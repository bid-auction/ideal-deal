package com.auction.bid.domain.product;

import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.query.sqm.mutation.internal.temptable.AfterUseAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private long id;

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
    private AuctionStatus auctionStatus;

    @Enumerated(EnumType.STRING)
    private AuctionPhase auctionPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private void setMember(Member member) {
        this.member = member;
    }

    public void assingMember(Member member){
        if(this.member == null){
            setMember(member);
        }
    }

    private void setCategory(Category category) {
        this.category = category;
    }

    public void assingCategory(Category category){
        if(this.category == null){
            setCategory(category);
        }
    }
}
