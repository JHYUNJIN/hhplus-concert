// src/main/java/kr/hhplus/be/common/exception/ApiException.java
package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException의 메시지로 ErrorCode의 메시지 사용
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage); // 상세 메시지가 있을 경우 사용
        this.errorCode = errorCode;
    }
}