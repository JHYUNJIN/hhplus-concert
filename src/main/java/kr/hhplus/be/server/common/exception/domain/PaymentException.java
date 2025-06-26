// src/main/java/kr/hhplus/be/common/exception/PaymentException.java
package kr.hhplus.be.server.common.exception.domain;

import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

public class PaymentException extends ApiException {
    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
    public PaymentException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}