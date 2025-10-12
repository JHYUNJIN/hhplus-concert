// src/main/java/kr/hhplus/be/common/exception/ConcertException.java
package kr.hhplus.be.user.common.exception;

public class CustomException extends kr.hhplus.be.user.common.exception.ApiException {
    public CustomException(ErrorCode errorCode) {
        super(errorCode);
    }
    public CustomException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}