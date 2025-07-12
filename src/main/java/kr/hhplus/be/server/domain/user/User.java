package kr.hhplus.be.server.domain.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record User (
        UUID id,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

   public User(BigDecimal amount, LocalDateTime createdAt, LocalDateTime updatedAt) {
           this(null, amount, createdAt, updatedAt);
       }

    public User charge(BigDecimal point) {
        return User.builder()
                .id(id)
                .amount(amount.add(point))
                .updatedAt(updatedAt)
                .build();
    }

    public User payment(BigDecimal balance) {
        return User.builder()
                .id(id)
                .amount(amount.subtract(balance))
                .updatedAt(updatedAt)
                .build();
    }

    public User refund(BigDecimal balance) {
        return User.builder()
                .id(id)
                .amount(amount.add(balance))
                .updatedAt(updatedAt)
                .build();
    }

    public boolean checkEnoughAmount(BigDecimal balance) {
        return amount.compareTo(balance) >= 0;
    }
}
