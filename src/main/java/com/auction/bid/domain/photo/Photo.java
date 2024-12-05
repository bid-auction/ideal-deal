package com.auction.bid.domain.photo;

import com.auction.bid.domain.product.Product;
import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="photo_id")
    private long id;

    @Column(nullable = false)
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;




}
