package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.member.Address;
import com.auction.bid.domain.member.Member;
import com.auction.bid.global.security.ConstSecurity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

public class SignUpDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{

        @NotEmpty
        private String loginId;

        @NotEmpty
        private String password;

        @NotEmpty
        private String email;

        @NotEmpty
        private String nickname;

        @NotEmpty
        private String name;

        @NotEmpty
        private String phoneNumber;

        @NotNull
        private Boolean emailVerified;

        @Valid
        @NotNull
        private Address address;


        public static Member toEntity(Request request, String encodedPassword) {
            return Member.builder()
                    .memberId(UUID.randomUUID())
                    .loginId(request.getLoginId())
                    .password(encodedPassword)
                    .email(request.getEmail())
                    .nickname(request.getNickname())
                    .name(request.getName())
                    .phoneNumber(request.getPhoneNumber())
                    .emailVerified(true)
                    .address(request.getAddress())
                    .provider("simple")
                    .role(ConstSecurity.ROLE_MEMBER)
                    .build();
        }

    }

    @Builder
    @Getter
    public static class Response {
        private Long id;
        private String loginId;
        private String name;
        private String nickname;

        public static Response fromEntity(Member member) {
            return Response.builder()
                    .id(member.getId())
                    .loginId(member.getLoginId())
                    .name(member.getName())
                    .nickname(member.getNickname())
                    .build();
        }
    }

}
