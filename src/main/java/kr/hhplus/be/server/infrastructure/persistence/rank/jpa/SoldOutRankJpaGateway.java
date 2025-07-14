package kr.hhplus.be.server.infrastructure.persistence.rank.jpa;

import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.rank.SoldOutRank;
import kr.hhplus.be.server.domain.rank.SoldOutRankRepository;
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
