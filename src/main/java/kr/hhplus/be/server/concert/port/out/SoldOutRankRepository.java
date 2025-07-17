package kr.hhplus.be.server.concert.port.out;

import kr.hhplus.be.server.concert.domain.SoldOutRank;

public interface SoldOutRankRepository {
    SoldOutRank save(SoldOutRank soldOutRank);
}
