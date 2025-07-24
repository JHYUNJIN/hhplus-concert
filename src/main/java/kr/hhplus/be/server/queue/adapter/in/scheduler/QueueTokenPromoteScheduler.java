package kr.hhplus.be.server.queue.adapter.in.scheduler;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QueueTokenPromoteScheduler {

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;

    /**
     * 대기열 토큰을 활성 토큰으로 승급하는 스케줄러
     * 5초마다 실행하여 만료된 활성 토큰 자리를 대기 토큰으로 채움
     */
    @Scheduled(fixedRate = 5000)
    public void promoteWaitingTokens() {
        LocalDateTime now = LocalDateTime.now(); // 현재 시간 가져오기, 다양한 시간대 문제를 피하기 위해 애플리케이션에서 직접 처리
        List<Concert> openConcerts = concertRepository.findByOpenConcerts(now);
        queueTokenRepository.promoteQueueToken(openConcerts);
    }
}