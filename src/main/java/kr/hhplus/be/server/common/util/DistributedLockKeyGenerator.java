package kr.hhplus.be.server.common.util;

import java.util.UUID;

/**
 * 분산 락 키를 생성하는 유틸리티 클래스입니다.
 * 키 생성 로직을 중앙에서 관리하여 일관성을 유지하고 실수를 방지합니다.
 */
public final class DistributedLockKeyGenerator {

    // 분산 락 키 접두사 상수 정의
    // 결제 도메인
    private static final String USER_LOCK_PREFIX = "user:"; // ⭐️ 결제 시 분산 락 잠금 키
    private static final String RESERVATION_LOCK_PREFIX = "reservation:"; // ⭐️ 결제 시 분산 락 잠금 키

    // 예약 도메인
    private static final String RESERVE_SEAT_LOCK_PREFIX = "reserve:seat:"; // ⭐️ 예약 시 분산 락 잠금 키
    private static final String EXPIRE_BATCH_LOCK_KEY = "reservation:expire-batch"; // ⭐️ 예약 만료 배치 락 키 추가

    // 더미 키 접두사
    private static final String DUMMY_UPDATE_SEATS_LOCK_KEY = "dummy-data:update-seat-counts"; // ⭐️ 더미 데이터 업데이트를 위한 잠금 키


    // 객체를 만들 필요가 없으므로 생성자 private 설정
    private DistributedLockKeyGenerator() {}

    // 결제 시
    public static String getUserLockKey(UUID userId) {
        return USER_LOCK_PREFIX + userId.toString();
    }
    public static String getReservationLockKey(UUID reservationId) {
        return RESERVATION_LOCK_PREFIX + reservationId.toString();
    }

    // 예약 시
    public static String getReserveSeatLockKey(UUID seatId) {
        return RESERVE_SEAT_LOCK_PREFIX + seatId.toString();
    }

    // 예약 만료 배치 시
    public static String getReservationExpireBatchLockKey() {
        return EXPIRE_BATCH_LOCK_KEY;
    }

    // 더미 데이터 업데이트 시
    public static String getDummyUpdateSeatsLockKey() {
        return DUMMY_UPDATE_SEATS_LOCK_KEY;
    }
}
