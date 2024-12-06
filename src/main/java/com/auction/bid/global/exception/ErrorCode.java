package com.auction.bid.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // MemberException
    NOT_EXIST_EMAIL("이메일이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_EXIST_EMAIL("이미 존재하는 이메일입니다.", HttpStatus.BAD_REQUEST),

    // MailException
    CAN_NOT_SEND_MAIL("메일을 전송하지 못했습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("메일을 인증하지 못했습니다.", HttpStatus.BAD_REQUEST),

    // AuthException
    AUTHENTICATION_FAILED("인증에 실패했습니다..", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND("해당 토큰은 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST),

    // ProductCreateException
    INVALID_AUCTION_START_TIME_NOW_AFTER("경매 시작 시간은 현재 시간 이후여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_AUCTION_END_TIME_START_AFTER("경매 종료 시간은 시작 시간 이후여야 합니다..", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("파일 업로드에 실패했습니다", HttpStatus.BAD_REQUEST),
    DUPLICATE_PRODUCT("이미 존재하는 상품입니다", HttpStatus.BAD_REQUEST);

    private final String description;
    private final HttpStatus httpStatus;
}
