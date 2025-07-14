package kr.hhplus.be.server.common.exception.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level; // SLF4J 로깅 레벨 import

@Getter
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
public enum ErrorCode {

    // Common Errors (범용 오류) - 대부분 ERROR 레벨
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다.", Level.ERROR), // 유효성 검사 실패
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다.", Level.ERROR),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다.", Level.WARN), // 권한 오류는 WARN으로 분류 가능
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 내부 오류가 발생했습니다.", Level.ERROR),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "유효하지 않은 타입의 값입니다.", Level.ERROR), // 타입 변환 실패
    NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청한 리소스를 찾을 수 없습니다.", Level.WARN), // 예상 가능한 리소스 없음
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C007", "인증 정보가 유효하지 않습니다.", Level.WARN), // 인증 오류는 WARN으로 분류 가능
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "C008", "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해주세요.", Level.WARN), // 비즈니스 흐름 중 발생
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "C009", "잘못된 요청입니다.", Level.ERROR), // 일반적인 잘못된 요청, ERROR로 분류

    // User Service Errors (사용자 관련 오류) - 대부분 WARN 레벨
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "U002", "잔액이 부족합니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "U003", "유효하지 않은 충전 금액입니다.", Level.WARN),
    INVALID_USE_AMOUNT(HttpStatus.BAD_REQUEST, "U004", "유효하지 않은 사용 금액입니다.", Level.WARN),
    NOT_ENOUGH_MIN_CHARGE_POINT(HttpStatus.BAD_REQUEST, "U005", "최소 충전 금액은 1000원 이상이어야 합니다.", Level.WARN),
    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "U006", "유효하지 않은 사용자 데이터입니다.", Level.WARN),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U007", "이미 존재하는 사용자입니다.", Level.WARN), // 비즈니스 예외
    USER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U008", "사용자 생성에 실패했습니다.", Level.ERROR), // 내부 오류, ERROR
    CHARGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U009", "충전에 실패했습니다.", Level.ERROR),

    // Concert Service Errors (콘서트 관련 오류) - 대부분 WARN 레벨
    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "콘서트를 찾을 수 없습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    CANNOT_RESERVATION_DATE(HttpStatus.BAD_REQUEST, "C002", "해당 날짜는 예약이 불가능 합니다.", Level.WARN),
    CONCERT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT003", "콘서트 생성에 실패했습니다.", Level.ERROR), // 내부 오류, ERROR
    CONCERT_DATE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CT004", "콘서트 날짜 생성에 실패했습니다.", Level.ERROR), // 내부 오류, ERROR
    CONCERT_DATE_ALREADY_EXISTS(HttpStatus.CONFLICT, "CT005", "해당 콘서트 날짜는 이미 존재합니다.", Level.WARN), // 비즈니스 예외
    CONCERT_DATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CT002", "콘서트 날짜를 찾을 수 없습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT003", "좌석을 찾을 수 없습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "CT004", "해당 좌석은 현재 예약할 수 없습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    SEAT_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "CT005", "해당 좌석은 이미 예약되었습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    ALREADY_RESERVED_SEAT(HttpStatus.BAD_REQUEST, "C006", "해당 좌석은 이미 예약되었습니다.", Level.WARN), // SEAT_ALREADY_RESERVED와 유사 (코드 중복 의심)
    SEAT_LOCK_CONFLICT(HttpStatus.CONFLICT, "C007", "이미 다른 사용자가 예약중입니다.", Level.WARN), // 동시성 처리 중 예상 가능한 경합
    OVER_DEADLINE(HttpStatus.BAD_REQUEST, "C004", "해당 날짜의 마감시간이 지났습니다.", Level.WARN), // 비즈니스 로직에 따른 마감
    SEAT_NOT_HOLD(HttpStatus.CONFLICT, "C008", "해당 좌석은 임시 배정되어있지 않습니다.", Level.WARN),

    // Reservation Service Errors (예약 관련 오류) - 대부분 WARN 레벨
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예약을 찾을 수 없습니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    RESERVATION_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "R002", "이미 확정된 예약입니다.", Level.WARN), // 예상 가능한 비즈니스 예외
    RESERVATION_INVALID_STATUS(HttpStatus.BAD_REQUEST, "R003", "현재 예약 상태에서는 요청을 처리할 수 없습니다.", Level.WARN),
    DEADLINE_PASSED(HttpStatus.BAD_REQUEST, "R004", "예약 마감 기한이 지났습니다.", Level.WARN), // 비즈니스 로직에 따른 마감
    RESERVATION_OWNERSHIP_MISMATCH(HttpStatus.FORBIDDEN, "R005", "예약 소유자가 일치하지 않습니다.", Level.WARN), // 권한/소유권 오류

    // Payment Service Errors (결제 관련 오류) - 대부분 WARN 레벨
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제에 실패했습니다.", Level.WARN), // 외부 결제 시스템 실패, 비즈니스 예외
    PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "P002", "결제 금액이 일치하지 않습니다.", Level.WARN),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "결제 내역을 찾을 수 없습니다.", Level.WARN),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "P004", "이미 처리된 결제입니다.", Level.WARN),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "P002", "결제 금액이 잘못되었습니다.", Level.WARN), // PAYMENT_INVALID_AMOUNT와 유사 (코드 중복 의심)
    ALREADY_PAID(HttpStatus.BAD_REQUEST, "P004", "이미 결제되었습니다.", Level.WARN), // PAYMENT_ALREADY_PROCESSED와 유사 (코드 중복 의심)
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "P005", "결제가 이미 처리되었습니다.", Level.WARN),

    // Ranking Service Errors (랭킹 관련 오류) - 대부분 WARN 레벨
    RANKING_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RK001", "랭킹 정보 갱신에 실패했습니다.", Level.ERROR),

    // Queue Service Errors (대기열 관련 오류) - 대부분 WARN 레벨
    INVALID_QUEUE_TOKEN(HttpStatus.BAD_REQUEST, "Q001", "대기열 토큰이 유효하지 않습니다.", Level.WARN),
    QUEUE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Q002", "대기열 토큰을 찾을 수 없습니다.", Level.WARN), // 락 처리 중 예상 가능한 경합
    QUEUE_TOKEN_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Q003", "대기열 토큰 직렬화 오류가 발생했습니다.", Level.ERROR),

    // Lock Service Errors (락 관련 오류) - 대부분 WARN 레벨
    LOCK_CONFLICT(HttpStatus.CONFLICT, "L001", "락 충돌이 발생했습니다.", Level.WARN),
    INVALID_SEAT_COUNT(HttpStatus.BAD_REQUEST, "L002", "좌석 수가 유효하지 않습니다.", Level.WARN),
    UNEXPECTED_RACE_CONDITION(HttpStatus.CONFLICT, "L003", "예상치 못한 경합 상태가 발생했습니다.", Level.ERROR);



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final Level logLevel; // 새로 추가된 필드

}