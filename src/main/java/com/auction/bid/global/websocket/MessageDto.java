package com.auction.bid.global.websocket;


import com.auction.bid.domain.member.Member;
import lombok.*;

import java.time.LocalDateTime;

public class MessageDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        private Long bidAmount;
        private Long maxBidLimit;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Response {
        private Long productId;
        private Long memberId;
        private String nickname;
        private Long bidAmount;
        private LocalDateTime bidTime;

        public static Response fromRequest(Member member, Long productId, Long bidAmount) {
            return Response.builder()
                    .productId(productId)
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .bidAmount(bidAmount)
                    .bidTime(LocalDateTime.now())
                    .build();
        }
    }

}