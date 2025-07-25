package kr.hhplus.be.server.queue.port.in;

import kr.hhplus.be.server.queue.domain.QueueToken;
import java.util.UUID;

public interface GetQueueInfoUseCase {
    QueueToken getQueueInfo(UUID concertId, String tokenId);
}