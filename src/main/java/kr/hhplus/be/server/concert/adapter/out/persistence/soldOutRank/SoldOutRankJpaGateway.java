package kr.hhplus.be.server.concert.adapter.out.persistence.soldOutRank;

import org.springframework.stereotype.Component;

import kr.hhplus.be.server.concert.domain.SoldOutRank;
import kr.hhplus.be.server.concert.port.out.SoldOutRankRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SoldOutRankJpaGateway implements SoldOutRankRepository {

    private final JpaSoldOutRankRepository jpaSoldOutRankRepository;

    @Override
    public SoldOutRank save(SoldOutRank soldOutRank) {
        return jpaSoldOutRankRepository.save(SoldOutRankEntity.from(soldOutRank))
                .toDomain();
    }
}
