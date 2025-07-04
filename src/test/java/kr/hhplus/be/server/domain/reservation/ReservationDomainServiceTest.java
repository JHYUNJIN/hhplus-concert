package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatGrade;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReservationDomainServiceTest {

    @InjectMocks
    private ReservationDomainService reservationDomainService;

    private UUID userId;
    private UUID concertId;
    private UUID concertDateId;
    private UUID seatId;
    private ConcertDate concertDate;
    private Seat availableSeat;

    @BeforeEach
    void beforeEach() {
        LocalDateTime now = LocalDateTime.now();
        userId = UUID.randomUUID();
        concertId = UUID.randomUUID();
        concertDateId = UUID.randomUUID();
        seatId = UUID.randomUUID();

        concertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .date(now.plusDays(7))
                .deadline(now.plusDays(5))
                .createdAt(now)
                .updatedAt(now)
                .build();

        availableSeat = Seat.builder()
                .id(seatId)
                .concertDateId(concertDateId)
                .seatNo(10)
                .price(BigDecimal.valueOf(50000))
                .seatGrade(SeatGrade.VIP)
                .status(SeatStatus.AVAILABLE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("예약_처리_성공")
    void processReservation_Success() throws CustomException {
        ReservationDomainResult result = reservationDomainService.processReservation(concertDate, availableSeat, userId);

        assertThat(result).isNotNull();

        assertThat(result.seat().status()).isEqualTo(SeatStatus.RESERVED);
        assertThat(result.seat().id()).isEqualTo(seatId);
        assertThat(result.seat().price()).isEqualTo(BigDecimal.valueOf(50000));

        assertThat(result.reservation().userId()).isEqualTo(userId);
        assertThat(result.reservation().seatId()).isEqualTo(seatId);
        assertThat(result.reservation().status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("예약_처리_실패_좌석이_예약불가능한_상태")
    void processReservation_Failure_SeatNotAvailable() throws CustomException {
        Seat reservedSeat = Seat.builder()
                .id(seatId)
                .concertDateId(concertDateId)
                .seatNo(10)
                .price(BigDecimal.valueOf(50000))
                .seatGrade(SeatGrade.VIP)
                .status(SeatStatus.RESERVED) // 이미 예약된 상태
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationDomainService.processReservation(concertDate, reservedSeat, userId));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.ALREADY_RESERVED_SEAT);
    }

    @Test
    @DisplayName("예약_처리_실패_좌석이_배정된_상태")
    void processReservation_Failure_SeatAssigned() throws CustomException {
        Seat assignedSeat = Seat.builder()
                .id(seatId)
                .concertDateId(concertDateId)
                .seatNo(10)
                .price(BigDecimal.valueOf(50000))
                .seatGrade(SeatGrade.VIP)
                .status(SeatStatus.ASSIGNED) // 좌석이 배정된 상태
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationDomainService.processReservation(concertDate, assignedSeat, userId));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.ALREADY_RESERVED_SEAT);
    }

    @Test
    @DisplayName("예약_처리_실패_콘서트_마감시간_초과")
    void processReservation_Failure_OverDeadline() throws CustomException {
        ConcertDate expiredConcertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .date(LocalDateTime.now().plusDays(7))
                .deadline(LocalDateTime.now().minusSeconds(1)) // 마감시간이 1초 지난 상태
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationDomainService.processReservation(expiredConcertDate, availableSeat, userId));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.OVER_DEADLINE);
    }

    @Test
    @DisplayName("예약_처리_성공_마감시간_경계값_테스트")
    void processReservation_Success_DeadlineBoundaryTest() throws CustomException {
        ConcertDate nearDeadlineConcertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .date(LocalDateTime.now().plusDays(7))
                .deadline(LocalDateTime.now().plusSeconds(1)) // 마감시간이 1초 남은 상태
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 마감시간이 임박한 상태에서도 예약이 성공해야 함, 예외가 발생하지 않아야 테스트가 통과함을 검증
        assertThatNoException().isThrownBy(() ->
                reservationDomainService.processReservation(nearDeadlineConcertDate, availableSeat, userId)
        );

        ConcertDate pastDeadlineConcertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .date(LocalDateTime.now().plusDays(7))
                .deadline(LocalDateTime.now().minusMinutes(1)) // 마감시간이 1분 지난 상태
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationDomainService.processReservation(pastDeadlineConcertDate, availableSeat, userId));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.OVER_DEADLINE);
    }
}