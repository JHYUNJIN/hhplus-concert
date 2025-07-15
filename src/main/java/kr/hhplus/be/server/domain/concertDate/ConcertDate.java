package kr.hhplus.be.server.domain.concertDate;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

// toBuilder=true를 추가하여 일부 필드만 변경된 새 객체를 쉽게 만들 수 있도록 합니다.
@Builder(toBuilder = true)
public record ConcertDate(
        UUID id,
        UUID concertId,
        Integer remainingSeatCount, // 현재 남은 좌석 수
        LocalDateTime date,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long availableSeatCount, // 현재 예약 가능한 좌석 수
        Long version // 낙관적 락을 위한 버전 필드
        ) {

    public boolean checkDeadline() {
        return deadline.isAfter(LocalDateTime.now()); // 현재 시간보다 마감 시간이 이후인지 확인
    }

    public ConcertDate decreaseAvailableSeatCount() throws CustomException {
        System.out.println("🚀[로그:정현진] availableSeatCount : " + this.availableSeatCount);
        if (this.availableSeatCount == null || this.availableSeatCount <= 0) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_SEAT, "남은 좌석이 없어 감소할 수 없습니다.");
        }
        // toBuilder를 사용하여 availableSeatCount만 변경된 새 record를 반환
        return this.toBuilder()
                .availableSeatCount(this.availableSeatCount - 1)
                .build();
    }

    public ConcertDate increaseAvailableSeatCount() {
        if (this.availableSeatCount == null) {
            // 초기값이 없는 경우를 대비
            return this.toBuilder().availableSeatCount(1L).build();
        }
        return this.toBuilder()
                .availableSeatCount(this.availableSeatCount + 1)
                .build();
    }
}