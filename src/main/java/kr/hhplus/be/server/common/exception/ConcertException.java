// src/main/java/kr/hhplus/be/common/exception/ConcertException.java
package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.exception.enums.ErrorCode;

public class ConcertException extends ApiException {
    public ConcertException(ErrorCode errorCode) {
        super(errorCode);
    }
    public ConcertException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}