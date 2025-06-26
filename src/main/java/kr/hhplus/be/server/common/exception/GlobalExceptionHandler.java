// src/main/java/kr/hhplus/be/common/exception/GlobalExceptionHandler.java
package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j; // SLF4J 로깅

@RestControllerAdvice // 모든 @Controller, @RestController에 적용
@Slf4j // 로깅 활성화
public class GlobalExceptionHandler {

    /**
     * @Valid 어노테이션을 사용한 유효성 검사 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @PathVariable 등에서 타입 변환 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("handleMethodArgumentTypeMismatchException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TYPE_VALUE);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 모든 커스텀 ApiException 처리
     */
    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        log.error("handleApiException", e);
        final ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getMessage()); // 상세 메시지 전달
        return new ResponseEntity<>(response, e.getErrorCode().getHttpStatus());
    }

    /**
     * 예상치 못한 모든 RuntimeException 처리 (서버 내부 오류)
     */
    @ExceptionHandler(Exception.class) // 가장 포괄적인 예외 처리
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}