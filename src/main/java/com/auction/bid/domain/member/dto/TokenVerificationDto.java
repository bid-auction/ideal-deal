package com.auction.bid.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class TokenVerificationDto {

    @Email(message = "유효한 이메일을 입력하세요.")
    private String email;

    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String token;

}
