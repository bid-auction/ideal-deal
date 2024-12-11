package com.auction.bid.global.exception.exceptions;

import com.auction.bid.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MemberException extends RuntimeException{

    private final ErrorCode errorCode;

    public MemberException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

}