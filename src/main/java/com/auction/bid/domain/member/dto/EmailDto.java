package com.auction.bid.domain.member.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class EmailDto {

    @Email(message = "유효한 이메일을 입력하세요.")
    private String email;

}
