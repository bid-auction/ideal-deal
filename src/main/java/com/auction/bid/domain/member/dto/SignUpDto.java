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

        @NotEmpty(message = "아이디를 입력해주세요.")
        private String loginId;

        @NotEmpty(message = "비밀번호를 입력해주세요.")
        private String password;

        @NotEmpty(message = "이메일을 입력해주세요.")
        private String email;

        @NotEmpty(message = "닉네임을 입력해주세요.")
        private String nickname;

        @NotEmpty(message = "이름을 입력해주세요.")
        private String name;

        @NotEmpty(message = "휴대폰 번호를 입력해주세요.")
        private String phoneNumber;

        @NotNull(message = "must not be null")
        private Boolean emailVerified;

        @Valid
        @NotNull
        private Address address;


        public static Member toEntity(Request request, String encodedPassword) {
            return Member.builder()
                    .memberUUID(UUID.randomUUID())
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
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
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
