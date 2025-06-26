// src/main/java/kr/hhplus/be/common/exception/ReservationException.java
package kr.hhplus.be.server.common.exception.domain;

import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

public class ReservationException extends ApiException {
    public ReservationException(ErrorCode errorCode) {
        super(errorCode);
    }
    public ReservationException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}