package kr.hhplus.be.server.external;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserApiClient {

    private final WebClient userServiceWebClient;

    /**
     * 사용자 잔액 조회
     * @param userId 사용자 ID
     * @return 사용자 잔액
     */
    public Mono<BigDecimal> getUserBalance(UUID userId) {
        return userServiceWebClient.get()
                .uri("/api/v1/users/{userId}/balance", userId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
                            }
                            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 클라이언트 오류: " + errorBody));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 서버 오류: " + errorBody))
                        )
                )
                .bodyToMono(BigDecimal.class);
    }

    /**
     * 사용자 잔액 충전
     * @param userId 사용자 ID
     * @param amount 충전 금액
     * @return 충전 후 잔액
     */
    public Mono<BigDecimal> chargeUserBalance(UUID userId, BigDecimal amount) {
        return userServiceWebClient.post()
                .uri("/api/v1/users/{userId}/charge", userId)
                .bodyValue(amount) // 요청 본문에 금액을 직접 전달
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
                            }
                            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 클라이언트 오류: " + errorBody));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 서버 오류: " + errorBody))
                        )
                )
                .bodyToMono(BigDecimal.class);
    }

    /**
     * 사용자 잔액 사용 (결제)
     * @param userId 사용자 ID
     * @param amount 사용 금액
     * @return 사용 후 잔액
     */
    public Mono<BigDecimal> useUserBalance(UUID userId, BigDecimal amount) {
        return userServiceWebClient.post()
                .uri("/api/v1/users/{userId}/use", userId)
                .bodyValue(amount) // 요청 본문에 금액을 직접 전달
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
                            }
                            if (clientResponse.statusCode() == HttpStatus.BAD_REQUEST) { // 잔액 부족 등
                                return Mono.error(new CustomException(ErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족합니다."));
                            }
                            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 클라이언트 오류: " + errorBody));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 서버 오류: " + errorBody))
                        )
                )
                .bodyToMono(BigDecimal.class);
    }

    /**
     * 사용자 잔액 환불 (결제 취소 시)
     * @param userId 사용자 ID
     * @param amount 환불 금액
     * @return 환불 후 잔액
     */
    public Mono<BigDecimal> refundUserBalance(UUID userId, BigDecimal amount) {
        return userServiceWebClient.post()
                .uri("/api/v1/users/{userId}/refund", userId)
                .bodyValue(amount) // 요청 본문에 금액을 직접 전달
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                                return Mono.error(new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
                            }
                            return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 클라이언트 오류: " + errorBody));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 서버 오류: " + errorBody))
                        )
                )
                .bodyToMono(BigDecimal.class);
    }

    /**
     * 사용자 존재 여부 확인
     * @param userId 사용자 ID
     * @return 사용자 존재 여부
     */
    public Mono<Boolean> checkUserExists(UUID userId) {
        return userServiceWebClient.head() // HEAD 요청으로 존재 여부만 확인
                .uri("/api/v1/users/{userId}", userId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.just(new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
                    }
                    return Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 클라이언트 오류"));
                })
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        Mono.error(new CustomException(ErrorCode.EXTERNAL_API_ERROR, "User Service 서버 오류"))
                )
                .toBodilessEntity() // 응답 본문 없이 헤더만 받음
                .map(response -> response.getStatusCode() == HttpStatus.OK)
                .onErrorResume(CustomException.class, e -> {
                    if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                        return Mono.just(false);
                    }
                    return Mono.error(e);
                });
    }
}
