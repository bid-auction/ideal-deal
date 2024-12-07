package com.auction.bid.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // ProductCreateException
    INVALID_AUCTION_START_TIME_NOW_AFTER("경매 시작 시간은 현재 시간 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_AUCTION_END_TIME_START_AFTER("경매 종료 시간은 시작 시간 이후여야 합니다..", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("파일 업로드중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_PRODUCT("이미 존재하는 상품입니다", HttpStatus.BAD_REQUEST);

    private final String description;
    private final HttpStatus httpStatus;
}
