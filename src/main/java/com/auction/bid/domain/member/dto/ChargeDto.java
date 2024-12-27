package com.auction.bid.domain.member.dto;

import lombok.*;

public class ChargeDto {

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request{
        private Long chargeMoney;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Response {
        private Long currMoney;
    }
}
