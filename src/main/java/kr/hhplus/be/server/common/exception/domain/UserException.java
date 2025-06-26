// src/main/java/kr/hhplus/be/common/exception/UserException.java
package kr.hhplus.be.server.common.exception.domain;

import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

public class UserException extends ApiException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
    public UserException(ErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}