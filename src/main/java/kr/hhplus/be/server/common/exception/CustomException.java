// src/main/java/kr/hhplus/be/common/exception/ConcertException.java
package kr.hhplus.be.server.common.exception;

public class CustomException extends ApiException {
    public CustomException(ErrorCode errorCode) {
        super(errorCode);
    }
    public CustomException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}