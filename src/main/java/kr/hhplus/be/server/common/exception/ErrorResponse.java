// src/main/java/kr/hhplus/be/common/exception/ErrorResponse.java
package kr.hhplus.be.server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp; // 오류 발생 시간
    private int status;              // HTTP 상태 코드 (숫자)
    private String error;            // HTTP 상태 코드 (문자열, 예: BAD_REQUEST)
    private String code;             // 정의된 내부 오류 코드 (예: U001)
    private String message;          // 클라이언트에게 보여줄 메시지
    private List<FieldErrorDetail> errors; // 유효성 검사 오류 (필드별 상세 정보)

    @Builder
    private ErrorResponse(int status, String error, String code, String message, List<FieldErrorDetail> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    // 기본 ErrorCode를 사용하여 ErrorResponse 생성
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // BindingResult (유효성 검사 오류)를 포함하여 ErrorResponse 생성
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().name())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(FieldErrorDetail.of(bindingResult))
                .build();
    }

    // 상세 메시지를 포함하여 ErrorResponse 생성 (커스텀 예외에서 사용)
    public static ErrorResponse of(ErrorCode errorCode, String detailMessage) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().name())
                .code(errorCode.getCode())
                .message(detailMessage != null && !detailMessage.isEmpty() ? detailMessage : errorCode.getMessage())
                .build();
    }

    // 유효성 검사 오류의 상세 정보를 위한 내부 클래스
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;    // 오류가 발생한 필드명
        private String value;    // 오류가 발생한 값
        private String reason;   // 오류 사유

        public static List<FieldErrorDetail> of(BindingResult bindingResult) {
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldErrorDetail(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }
}