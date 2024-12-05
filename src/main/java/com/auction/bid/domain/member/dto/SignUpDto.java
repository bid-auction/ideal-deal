package com.auction.bid.domain.member.dto;

import com.auction.bid.domain.member.Address;
import com.auction.bid.domain.member.Member;
import com.auction.bid.global.security.ConstSecurity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class SignUpDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{

        @NotEmpty
        private String email;

        @NotEmpty
        private String password;

        @NotEmpty
        private String nickname;

        @NotEmpty
        private String username;

        @NotEmpty
        private String phoneNumber;

        @NotNull
        private Boolean emailVerified;

        @Valid
        @NotNull
        private Address address;


        public static Member toEntity(Request request, String encodedPassword) {
            return Member.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .nickname(request.nickname)
                    .username(request.username)
                    .phoneNumber(request.phoneNumber)
                    .address(request.address)
                    .emailVerified(true)
                    .role(ConstSecurity.ROLE_MEMBER)
                    .build();
        }

    }

    @Builder
    @Getter
    public static class Response {
        private Long id;
        private String username;
        private String nickname;

        public static Response fromEntity(Member member) {
            return Response.builder()
                    .id(member.getId())
                    .username(member.getUsername())
                    .nickname(member.getNickname())
                    .build();
        }
    }

}
