package kr.hhplus.be.server.domain.event.rank;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.event.payment.PaymentSuccessEvent;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.rank.ConcertSoldOutManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertSoldOutRankListener {

    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;
    private final ConcertSoldOutManager concertSoldOutManager;

    /**
     * 결제 성공시 이벤트 수신
     * 1. 해당 콘서트 매진 체크
     * 2. 매진시 랭킹 업데이트
     *
     * @param event 결제 성공 이벤트 정보
     */
    @Async
    @TransactionalEventListener
    public void handleEvent(PaymentSuccessEvent event) {
        log.info("🚀[로그:정현진] 결제 성공 이벤트 발생: {}", event);
        try {
            ConcertDate concertDate = getConcertDate(event.seat().concertDateId());

            // 모든 좌석이 매진 되었는지 확인
            List<Seat> allSeats = seatRepository.findByConcertDateId(concertDate.id());
            boolean isAllSeatsAssigned = allSeats.stream()
                    .allMatch(seatItem -> seatItem.status() == SeatStatus.ASSIGNED);

            if (!isAllSeatsAssigned)
                return;

            // 매진된 경우 랭킹 업데이트
            concertSoldOutManager.processUpdateRanking(event, concertDate.concertId(), allSeats.size());

        } catch (Exception e) {
            // TODO: 실패한 이벤트 재시도 OR 예외 처리?
        }
    }

    private ConcertDate getConcertDate(UUID concertDateId) throws CustomException {
        return concertDateRepository.findById(concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND));
    }
}
