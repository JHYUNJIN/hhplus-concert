package kr.hhplus.be.server.external.dataplatform.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.external.dataplatform.port.in.PaymentSuccessUseCase;
import kr.hhplus.be.server.external.dataplatform.port.out.DataPlatformOutPort;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataPlatformService implements PaymentSuccessUseCase {

	private final DataPlatformOutPort dataPlatformOutPort;
	private final ConcertRepository concertRepository;
	private final ConcertDateRepository concertDateRepository;


	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void sendDataPlatform(PaymentSuccessEvent event) {
		try {
			ConcertDate concertDate = concertDateRepository.findById(event.seat().concertDateId())
				.orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND,"콘서트 날짜를 찾을 수 없습니다. ID: " + event.seat().concertDateId()));
			Concert concert = concertRepository.findById(concertDate.concertId())
					.orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND,"콘서트를 찾을 수 없습니다. ID: " + concertDate.concertId()));
			dataPlatformOutPort.send(event, concert);
		} catch (Exception e) {
			log.error("데이터 플랫폼 전송 중 오류 발생. Event: {}", event, e);
			// TODO 실패시 재시도 처리
			// 예: 재시도 로직, Dead Letter Queue로 전송 등
		}
	}
}
