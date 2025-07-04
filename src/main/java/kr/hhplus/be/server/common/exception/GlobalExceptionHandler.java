// src/main/java/kr/hhplus/be/common/exception/GlobalExceptionHandler.java
package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import org.slf4j.event.Level;
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
        ErrorCode errorCode = e.getErrorCode();
        Level logLevel = errorCode.getLogLevel();

        String logMessage = String.format("handleApiException - ErrorCode: %s, Message: %s",
                errorCode.getCode(), e.getMessage());

        if (logLevel == Level.ERROR) {
            log.error(logMessage, e); // ERROR 레벨일 경우 스택 트레이스 포함
        } else if (logLevel == Level.WARN) {
            log.warn(logMessage); // WARN 레벨일 경우 메시지만
        } else if (logLevel == Level.INFO) {
            log.info(logMessage); // INFO 레벨일 경우 메시지만
        } else {
            // 그 외의 레벨 (DEBUG, TRACE 등)은 기본 로거 설정에 따르거나, 필요 시 추가 로직 구현
            log.debug(logMessage);
        }

        final ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
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