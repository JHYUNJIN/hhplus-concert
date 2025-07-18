package kr.hhplus.be.server.user.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "amount", precision = 10, nullable = false)
    @ColumnDefault("0")
    private BigDecimal amount;

    public static UserEntity from(User user) {
        return UserEntity.builder()
                .id(user.id() != null ? user.id().toString() : null)
                .amount(user.amount())
                .build();
    }

    public User toDomain() {
        return User.builder()
                .id(UUID.fromString(id))
                .amount(amount)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
