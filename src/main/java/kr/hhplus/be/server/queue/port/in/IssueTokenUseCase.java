package kr.hhplus.be.server.queue.port.in;

import kr.hhplus.be.server.queue.domain.QueueToken;
import java.util.UUID;

public interface IssueTokenUseCase {
    QueueToken issueQueueToken(UUID userId, UUID concertId);
}
