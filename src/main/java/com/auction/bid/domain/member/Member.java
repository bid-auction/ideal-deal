package com.auction.bid.domain.member;

import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(
        name = "member",
        indexes = @Index(name = "idx_email", columnList = "email", unique = true)
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String password;
    private String phoneNumber;
    private String nickname;
    private String username;

    @Embedded
    private Address address;

    private boolean emailVerified;
    private boolean phoneVerified;

    private int balance;
    private String role;
}
