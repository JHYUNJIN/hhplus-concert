// src/main/java/kr/hhplus/be/common/exception/ErrorCode.java
package kr.hhplus.be.server.common.exception.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (범용 오류)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "유효하지 않은 타입의 값입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청한 리소스를 찾을 수 없습니다."), // 리소스를 찾을 수 없을 때 범용적으로 사용
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C007", "인증 정보가 유효하지 않습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "C008", "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해주세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C009", "잘못된 요청입니다."), // 특정 상황에 맞지 않는 일반적인 BAD_REQUEST

    // User Service Errors (사용자 관련 오류)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "U002", "잔액이 부족합니다."),
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "U003", "유효하지 않은 충전 금액입니다."),
    INVALID_USE_AMOUNT(HttpStatus.BAD_REQUEST, "U004", "유효하지 않은 사용 금액입니다."),

    // Concert Service Errors (콘서트 관련 오류)
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "콘서트를 찾을 수 없습니다."),
    CANNOT_RESERVATION_DATE(HttpStatus.CONFLICT, "C002", "해당 날짜는 예약이 불가능 합니다."),
    CONCERT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT003", "콘서트 생성에 실패했습니다."), // 콘서트 생성 실패
    CONCERT_DATE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT004", "콘서트 날짜 생성에 실패했습니다."), // 콘서트 날짜 생성 실패
    CONCERT_DATE_ALREADY_EXISTS(HttpStatus.CONFLICT, "CT005", "해당 콘서트 날짜는 이미 존재합니다."), // 콘서트 날짜 중복
    CONCERT_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CT002", "콘서트 날짜를 찾을 수 없습니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT003", "좌석을 찾을 수 없습니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "CT004", "해당 좌석은 현재 예약할 수 없습니다."), // CONFLICT로 변경
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "CT005", "해당 좌석은 이미 예약되었습니다."), // 추가

    // Reservation Service Errors (예약 관련 오류)
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "R002", "이미 확정된 예약입니다."),
    RESERVATION_INVALID_STATUS(HttpStatus.BAD_REQUEST, "R003", "현재 예약 상태에서는 요청을 처리할 수 없습니다."),
    DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "R004", "예약 마감 기한이 지났습니다."),
    RESERVATION_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN, "R005", "예약 소유자가 일치하지 않습니다."), // 추가

    // Payment Service Errors (결제 관련 오류)
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제에 실패했습니다."),
    PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "P002", "결제 금액이 일치하지 않습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "결제 내역을 찾을 수 없습니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "P004", "이미 처리된 결제입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}