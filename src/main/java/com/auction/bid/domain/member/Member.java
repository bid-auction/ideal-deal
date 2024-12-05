package com.auction.bid.domain.member;

import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")

    private Long id;

    private String email;
    private String password;
    private String phoneNumber;
    private String nickname;

    @Embedded
    private Address address;

    private boolean emailVerified;
    private boolean phoneVerified;

    private int balance;
    private String role;
}
