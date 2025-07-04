package kr.hhplus.be.server.usecase.concert;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatGrade;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class ConcertServiceTest {

    @InjectMocks // ConcertService의 Mock 객체 생성
    private ConcertService concertService;

    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private ConcertDateRepository concertDateRepository;
    @Mock
    private SeatRepository seatRepository;

    // 테스트에 사용할 콘서트 정보
    private UUID concertId;
    private UUID concertDateId;
    private UUID seatId;
    private Concert concert;
    private ConcertDate concertDate;
    private Seat seat;

    // 테스트에 사용할 콘서트 정보 초기화
    @BeforeEach
    void beforeEach() {
        concertId = UUID.randomUUID();
        concertDateId = UUID.randomUUID();
        seatId = UUID.randomUUID();

        concert = Concert.builder()
                .id(concertId)
                .title("GD 콘서트")
                .artist("GD")
                .build();

        concertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .date(LocalDateTime.now().plusDays(7)) // 콘서트 날짜
                .deadline(LocalDateTime.now().plusDays(5)) // 예약 마감일
                .build();

        seat = Seat.builder() // 콘서트 날짜에 대한 좌석 정보
                .id(seatId)
                .concertDateId(concertDateId)
                .seatNo(1) // 좌석 번호
                .seatGrade(SeatGrade.VIP) // 좌석 등급
                .status(SeatStatus.AVAILABLE) // 좌석 상태
                .price(BigDecimal.valueOf(100000)) // 좌석 가격
                .build();
    }

    @Test
    @DisplayName("예약_가능_콘서트_날짜_조회_성공")
    void getAvailableConcertDates_Success() throws CustomException {
        List<ConcertDate> concertDateEntities = List.of(concertDate);

        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(concertDateRepository.findAvailableDatesWithAvailableSeatCount(concertId)).thenReturn(concertDateEntities);

        List<ConcertDate> results = concertService.getAvailableConcertDates(concertId);

        verify(concertRepository, times(1)).existsById(concertId);
        verify(concertDateRepository, times(1)).findAvailableDatesWithAvailableSeatCount(concert.id());

        assertThat(results).hasSize(1); // ID로 조회했으니 결과는 1개가 나와야 함
        assertThat(results.get(0).id()).isEqualTo(concertDateId); // 결과가 1개이므로 리스트의 0번째 인덱스 조회
    }

    @Test
    @DisplayName("예약_가능_콘서트_날짜_조회_실패_콘서트_조회_실패")
    void getAvailableConcertDates_Failure_ConcertNotFound() {
        when(concertRepository.existsById(concertId)).thenReturn(false); // 콘서트 조회 실패 가정

        CustomException customException = assertThrows(CustomException.class,
                () -> concertService.getAvailableConcertDates(concertId));

        verify(concertRepository, times(1)).existsById(concertId);
        verify(concertDateRepository, never()).findAvailableDatesWithAvailableSeatCount(concert.id()); // 콘서트를 조회하지 못했으므로 콘서트 날짜 조회는 호출되지 않아야 함

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test // 콘서트는 존재하지만, 예약 가능한 날짜가 없습니다.
    @DisplayName("예약_가능_콘서트_날짜_조회_정상_빈_리스트(예약 가능한 날짜가 없는 경우)")
    void getAvailableConcertDates_Success_CanReservationDateNotFound() throws CustomException {
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(concertDateRepository.findAvailableDatesWithAvailableSeatCount(concertId))
                .thenReturn(Collections.emptyList()); // 예약 가능한 콘서트 날짜가 없는 경우

        List<ConcertDate> results = concertService.getAvailableConcertDates(concertId);

        verify(concertRepository, times(1)).existsById(concertId);
        verify(concertDateRepository, times(1)).findAvailableDatesWithAvailableSeatCount(concertId);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("예약_가능_좌석_조회_정상")
    void getAvailableSeats_Success() throws CustomException {
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(seatRepository.findAvailableSeats(concertId, concertDateId)).thenReturn(List.of(seat));

        List<Seat> results = concertService.getAvailableSeats(concertId, concertDateId);

        verify(concertRepository, times(1)).existsById(concertId);
        verify(seatRepository, times(1)).findAvailableSeats(concertId, concertDateId);
        verify(concertDateRepository, never()).existsById(concertId); // 예약 가능한 좌석이 없을 경우에만 조회되므로 호출되지 않아야 함

        assertThat(results).hasSize(1); // 좌석 ID로 조회했으므로 결과는 1개가 나와야 함
        assertThat(results.get(0).id()).isEqualTo(seatId);
    }

    @Test
    @DisplayName("예약_가능_좌석_조회_실패_콘서트찾을수없음")
    void getAvailableSeats_Failure_ConcertNotFound() {
        when(concertRepository.existsById(concertId)).thenReturn(false);

        CustomException customException = assertThrows(CustomException.class,
                () -> concertService.getAvailableSeats(concertId, concertDateId));

        verify(concertRepository, times(1)).existsById(concertId);
        verify(seatRepository, never()).findAvailableSeats(concertId, concertDateId); // 콘서트를 조회하지 못했으므로 좌석 조회는 호출되지 않아야 함
        verify(concertDateRepository, never()).existsById(concertDateId);

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test // 콘서트는 존재하지만, 좌석도 없고, 콘서트 날짜도 존재하지 않는 경우
    @DisplayName("예약_가능_좌석_조회_실패_해당날짜예약불가")
    void getAvailableSeats_Failure_CannotReservationDate() {
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(seatRepository.findAvailableSeats(concertId, concertDateId)).thenReturn(Collections.emptyList()); // 예약 가능한 좌석이 없는 경우
        when(concertDateRepository.existsById(concertDateId)).thenReturn(false);

        CustomException customException = assertThrows(CustomException.class,
                () -> concertService.getAvailableSeats(concertId, concertDateId));

        verify(concertRepository, times(1)).existsById(concertId);
        verify(seatRepository, times(1)).findAvailableSeats(concertId, concertDateId);
        verify(concertDateRepository, times(1)).existsById(concertDateId);

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CANNOT_RESERVATION_DATE);
    }

    @Test // 콘서트 날짜가 존재하지만, 예약 가능한 좌석이 없는 경우
    @DisplayName("예약_가능_좌석_조회_정상_빈_리스트(매진, 예약)")
    void getAvailableSeats_Success_EmptyList() throws CustomException {
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(seatRepository.findAvailableSeats(concertId, concertDateId)).thenReturn(Collections.emptyList());
        when(concertDateRepository.existsById(concertDateId)).thenReturn(true);

        List<Seat> results = concertService.getAvailableSeats(concertId, concertDateId);

        verify(concertRepository, times(1)).existsById(concertId);
        verify(seatRepository, times(1)).findAvailableSeats(concertId, concertDateId);
        verify(concertDateRepository, times(1)).existsById(concertDateId);

        assertThat(results).isEmpty();
    }

}
