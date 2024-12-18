package com.auction.bid.domain.member;


import com.auction.bid.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "member",
        indexes = {
                @Index(name = "idx_provider_id", columnList = "provider_id"),
                @Index(name = "idx_member_uuid", columnList = "member_uuid")
        }
)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_uuid", columnDefinition = "BINARY(16)", unique = true)
    private UUID memberUUID;

    @Column(name = "name")
    private String name;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "password")
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "balance")
    private int balance;

    @Column(name = "role")
    private String role;

    @Embedded
    private Address address;

}
