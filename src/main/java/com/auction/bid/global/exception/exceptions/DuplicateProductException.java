package com.auction.bid.global.exception.exceptions;

import com.auction.bid.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateProductException extends RuntimeException {

    private final ErrorCode errorCode;

    public DuplicateProductException(ErrorCode errorCode){
        super(errorCode.getDescription());
        this.errorCode=errorCode;

    }
}
