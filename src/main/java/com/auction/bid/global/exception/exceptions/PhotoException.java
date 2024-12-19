package com.auction.bid.global.exception.exceptions;

import com.auction.bid.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class PhotoException extends RuntimeException{

    private final ErrorCode errorCode;

    public PhotoException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

}