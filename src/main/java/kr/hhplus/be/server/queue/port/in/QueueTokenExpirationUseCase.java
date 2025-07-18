package kr.hhplus.be.server.queue.port.in;

public interface QueueTokenExpirationUseCase {
    void expiresQueueToken(String tokenId);
}