package com.auction.bid.global.exception.exceptions;

import com.auction.bid.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MoneyException extends RuntimeException{

    private final ErrorCode errorCode;

    public MoneyException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

}