package kr.hhplus.be.server.usecase.queue.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueTokenPromoteScheduler {

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;

    /**
     * 대기열 토큰을 활성 토큰으로 승급하는 스케줄러
     * 5초마다 실행하여 만료된 활성 토큰 자리를 대기 토큰으로 채움
     */
    @Scheduled(fixedRate = 10000)
    public void promoteWaitingTokens() {
        System.out.println("🚀[로그:정현진] 스케줄러 실행: 대기열 토큰 승급 시작 promoteWaitingTokens");
        List<Concert> openConcerts = concertRepository.findByOpenConcerts();
        log.info("openConcerts title: {}", openConcerts.stream()
                .map(concert -> concert.title())
                .toList());
        queueTokenRepository.promoteQueueToken(openConcerts);
    }
}