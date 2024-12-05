package com.auction.bid.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class EmailDto {

    @Email(message = "유효한 이메일을 입력하세요.")
    private String email;

}
