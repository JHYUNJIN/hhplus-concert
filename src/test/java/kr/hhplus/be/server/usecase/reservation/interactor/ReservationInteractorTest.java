package kr.hhplus.be.server.usecase.reservation.interactor;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import kr.hhplus.be.server.reservation.usecase.ReservationInteractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.enums.SeatGrade;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.reservation.domain.ReservationCreatedEvent;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.in.dto.ReservationDomainResult;
import kr.hhplus.be.server.reservation.usecase.ReservationDomainService;
import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.payment.port.out.EventPublisher;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import kr.hhplus.be.server.reservation.port.out.SeatLockRepository;
import kr.hhplus.be.server.reservation.port.in.dto.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.port.in.ReservationOutput;
import kr.hhplus.be.server.reservation.port.in.ReserveSeatResult;

@ExtendWith(MockitoExtension.class)
public class ReservationInteractorTest {

    @InjectMocks
    private ReservationInteractor reservationInteractor;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private QueueTokenRepository queueTokenRepository;
    @Mock
    private SeatLockRepository seatLockRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ConcertDateRepository concertDateRepository;
    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private ReservationOutput reservationOutput;
    @Mock
    private ReservationDomainService reservationDomainService;
    @Mock
    private EventPublisher eventPublisher;

    // 콘서트 예약 테스트에 사용할 데이터
    private UUID concertId;
    private UUID concertDateId;
    private UUID seatId;
    private UUID userId;
    private UUID queueTokenId;
    private UUID reservationId;
    private UUID paymentId;
    private ReserveSeatCommand command;
    private QueueToken queueToken;
    private Seat seat; // 예약할 좌석
    private Seat reservedSeat; // 예약된 좌석
    private ConcertDate concertDate;
    private Reservation reservation;
    private Payment payment;
    private ReservationDomainResult reservationDomainResult;

    @BeforeEach
    void beforeEach() {
        LocalDateTime now = LocalDateTime.now();
        concertId = UUID.randomUUID();
        concertDateId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        userId = UUID.randomUUID();
        queueTokenId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        command = new ReserveSeatCommand(concertId, concertDateId, seatId, queueTokenId.toString()); // 예약을 하기 위한 커맨드 객체
        queueToken = QueueToken.activeTokenOf(queueTokenId, userId, concertId, 1000000); // 활성토큰
        seat = new Seat(seatId, concertDateId, 10, BigDecimal.valueOf(100000), SeatGrade.VIP, SeatStatus.AVAILABLE, now,
                now); // 좌석 ID, 콘서트 날짜 ID, 좌석 번호, 가격, 좌석 등급, 좌석 상태, 생성일시, 수정일시
        reservedSeat = seat.reserve(); // 예약하기 -> 좌석을 예약 상태로 변경
        concertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .remainingSeatCount(50)
                .date(now.plusDays(7))
                .deadline(now.plusDays(5))
                .createdAt(now)
                .updatedAt(now)
                .availableSeatCount(50L)
                .version(0L)
                .build();
        reservation = new Reservation(reservationId, userId, seatId, ReservationStatus.PENDING, now, now, now.plusMinutes(5));
        payment = new Payment(paymentId, userId, reservationId, BigDecimal.valueOf(100000), PaymentStatus.PENDING, null,
                now, now); // 결제 ID, 사용자 ID, 예약 ID, 결제 금액, 결제 상태, 결제 승인 코드, 생성일시, 수정일시
        reservationDomainResult = new ReservationDomainResult(reservedSeat, reservation,payment,concertDate); // 예약 도메인 결과 객체
    }

    @Test
    @DisplayName("콘서트_좌석_예약_성공")
    void concertSeatReservation_Success() throws CustomException {
        when(queueTokenRepository.findQueueTokenByTokenId(queueTokenId.toString())).thenReturn(queueToken); // 대기열 토큰 조회
        when(concertRepository.existsById(command.concertId())).thenReturn(true); // 콘서트 존재 여부 확인
        when(concertDateRepository.findById(command.concertDateId())).thenReturn(Optional.of(concertDate)); // 콘서트 날짜 조회
        when(seatRepository.findBySeatIdAndConcertDateId(command.seatId(), command.concertDateId()))
                .thenReturn(Optional.of(seat)); // 좌석 조회
        when(seatLockRepository.acquisitionLock(command.seatId())).thenReturn(true); // 좌석 잠금 획득 성공
        when(reservationDomainService.processReservation(concertDate, seat, userId)).thenReturn(reservationDomainResult); // 좌석 예약 결과 반환
        when(seatRepository.save(reservedSeat)).thenReturn(reservedSeat); // 좌석(DB) 예약 처리
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation); // 예약(DB) 예약 처리
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment); // 결제(DB) 예약 처리

        // When
        reservationInteractor.reserveSeat(command);

        // Then
        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(queueTokenId.toString());
        verify(concertRepository, times(1)).existsById(command.concertId());
        verify(concertDateRepository, times(1)).findById(command.concertDateId());
        verify(seatRepository, times(1)).findBySeatIdAndConcertDateId(command.seatId(), command.concertDateId());
        verify(seatLockRepository, times(1)).acquisitionLock(command.seatId());
        verify(reservationDomainService, times(1)).processReservation(concertDate, seat, userId);
        verify(seatRepository, times(1)).save(reservedSeat);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(seatHoldRepository, times(1)).hold(seatId, userId);
        verify(eventPublisher, times(1)).publish(any(ReservationCreatedEvent.class));
        verify(reservationOutput, times(1)).ok(any(ReserveSeatResult.class));
        verify(seatLockRepository, times(1)).releaseLock(seatId); // 좌석 잠금 해제
        verify(queueTokenRepository, times(1)).expiresQueueToken(queueTokenId.toString()); // 대기열 토큰 만료 처리
    }

    @Test
    @DisplayName("콘서트_좌석_예약_실패_대기열토큰유효하지않음")
    void concertSeatReservation_Failure_InvalidQueueToken() throws CustomException {
        QueueToken waitingToken = QueueToken.waitingTokenOf(queueTokenId, userId, concertId, 10);
        when(queueTokenRepository.findQueueTokenByTokenId(queueTokenId.toString())).thenReturn(waitingToken);

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationInteractor.reserveSeat(command));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(queueTokenId.toString());
        verify(concertRepository, never()).existsById(any());
        verify(concertDateRepository, never()).findById(any());
        verify(seatRepository, never()).findBySeatIdAndConcertDateId(any(), any());
        verify(seatLockRepository, never()).acquisitionLock(any());
        verify(reservationDomainService, never()).processReservation(any(), any(), any());
        verify(seatRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(seatHoldRepository, never()).hold(any(), any());
        verify(eventPublisher, never()).publish(any(ReservationCreatedEvent.class));
        verify(seatLockRepository, never()).releaseLock(any());
        verify(reservationOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_QUEUE_TOKEN);
    }

    @Test
    @DisplayName("콘서트_좌석_예약_실패_콘서트못찾음")
    void concertSeatReservation_Failure_ConcertNotFound() throws CustomException {
        when(queueTokenRepository.findQueueTokenByTokenId(queueTokenId.toString())).thenReturn(queueToken);
        when(concertRepository.existsById(command.concertId())).thenReturn(false);

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationInteractor.reserveSeat(command));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(queueTokenId.toString());
        verify(concertRepository, times(1)).existsById(command.concertId());
        verify(concertDateRepository, never()).findById(any());
        verify(seatRepository, never()).findBySeatIdAndConcertDateId(any(), any());
        verify(seatLockRepository, never()).acquisitionLock(any());
        verify(reservationDomainService, never()).processReservation(any(), any(), any());
        verify(seatRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(seatHoldRepository, never()).hold(any(), any());
        verify(eventPublisher, never()).publish(any(ReservationCreatedEvent.class));
        verify(seatLockRepository, never()).releaseLock(any());
        verify(reservationOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }

    @Test
    @DisplayName("콘서트_좌석_예약_실패_콘서트날짜못찾음")
    void concertSeatReservation_Failure_ConcertDateNotFound() throws CustomException {
        when(queueTokenRepository.findQueueTokenByTokenId(queueTokenId.toString())).thenReturn(queueToken);
        when(concertRepository.existsById(command.concertId())).thenReturn(true);
        when(concertDateRepository.findById(command.concertDateId())).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationInteractor.reserveSeat(command));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(queueTokenId.toString());
        verify(concertRepository, times(1)).existsById(command.concertId());
        verify(concertDateRepository, times(1)).findById(command.concertDateId());
        verify(seatRepository, never()).findBySeatIdAndConcertDateId(any(), any());
        verify(seatLockRepository, never()).acquisitionLock(any());
        verify(reservationDomainService, never()).processReservation(any(), any(), any());
        verify(seatRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(seatHoldRepository, never()).hold(any(), any());
        verify(eventPublisher, never()).publish(any(ReservationCreatedEvent.class));
        verify(seatLockRepository, never()).releaseLock(any());
        verify(reservationOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CONCERT_DATE_NOT_FOUND);
    }

    @Test
    @DisplayName("콘서트_좌석_예약_실패_좌석락획득실패")
    /*
    좌석 락(Seat Lock) 획득에 실패하는 경우는 주로 다음과 같은 상황에서 발생할 수 있습니다.

    동시성 이슈: 여러 사용자가 동시에 같은 좌석을 예약하려고 할 때, 먼저 락을 획득한 사용자만 예약이 가능하고, 나머지는 락 획득에 실패합니다.
    이미 예약 처리 중: 다른 트랜잭션에서 해당 좌석에 대한 예약 처리가 진행 중이라면, 락이 이미 잡혀 있어서 추가로 락을 획득할 수 없습니다.
    락 시스템 장애: 분산 락 시스템(Redis, DB 등)에 일시적인 장애가 발생해 락 획득이 실패할 수 있습니다.
    락 만료 지연: 이전 예약 시도에서 락이 정상적으로 해제되지 않아, 잠시 동안 락이 걸려 있는 경우에도 실패할 수 있습니다.
    이런 상황은 주로 동시성 제어와 관련된 이슈에서 자주 발생합니다.
     */
    void concertSeatReservation_Failure_getSeatLockFail() throws CustomException {
        when(queueTokenRepository.findQueueTokenByTokenId(queueTokenId.toString())).thenReturn(queueToken);
        when(concertRepository.existsById(command.concertId())).thenReturn(true);
        when(concertDateRepository.findById(command.concertDateId())).thenReturn(Optional.of(concertDate));
        when(seatRepository.findBySeatIdAndConcertDateId(command.seatId(), command.concertDateId()))
                .thenReturn(Optional.of(seat));
        when(seatLockRepository.acquisitionLock(command.seatId())).thenReturn(false);

        CustomException customException = assertThrows(CustomException.class,
                () -> reservationInteractor.reserveSeat(command));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.SEAT_LOCK_CONFLICT);

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(queueTokenId.toString());
        verify(concertRepository, times(1)).existsById(command.concertId());
        verify(concertDateRepository, times(1)).findById(command.concertDateId());
        verify(seatRepository, times(1)).findBySeatIdAndConcertDateId(command.seatId(), command.concertDateId());
        verify(seatLockRepository, times(1)).acquisitionLock(command.seatId());
        verify(reservationDomainService, never()).processReservation(any(), any(), any());
        verify(seatRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(seatHoldRepository, never()).hold(any(), any());
        verify(eventPublisher, never()).publish(any(ReservationCreatedEvent.class));
        verify(seatLockRepository, never()).releaseLock(any());
        verify(reservationOutput, never()).ok(any());
        verify(queueTokenRepository, times(1)).expiresQueueToken(queueTokenId.toString()); // 대기열 토큰 만료 처리
    }

}
