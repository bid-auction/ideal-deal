package com.auction.bid.domain.member.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class LoginDto {

    @NotEmpty(message = "아이디를 입력하세요.")
    private String loginId;

    @NotEmpty(message = "비밀번호를 입력해주세요.")
    private String password;

}
