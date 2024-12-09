package com.auction.bid.global.exception.exceptions;

import com.auction.bid.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ProductException extends RuntimeException {

    private final ErrorCode errorCode;

    public ProductException(ErrorCode errorCode){
        super(errorCode.getDescription());
        this.errorCode=errorCode;

<<<<<<< HEAD:src/main/java/com/auction/bid/global/exception/exceptions/ProductException.java
    }
}
=======
}
>>>>>>> develop:src/main/java/com/auction/bid/global/exception/exceptions/AuthException.java
