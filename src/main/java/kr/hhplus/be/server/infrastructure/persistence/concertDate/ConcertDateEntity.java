package kr.hhplus.be.server.infrastructure.persistence.concertDate;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.infrastructure.persistence.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONCERT_DATE",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_CONCERT_DATE_CONCERT_ID_DATE", columnNames = {"concert_id", "date"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class ConcertDateEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private String id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "concert_id", length = 36, nullable = false)
    private String concertId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    public static ConcertDateEntity from(ConcertDate concertDate) {
        return ConcertDateEntity.builder()
                .id(concertDate.id() != null ? concertDate.id().toString() : null)
                .concertId(concertDate.concertId().toString())
                .date(concertDate.date())
                .deadline(concertDate.deadline())
                .build();
    }

    public ConcertDate toDomain() {
        return ConcertDate.builder()
                .id(UUID.fromString(id))
                .concertId(UUID.fromString(concertId))
                .date(date)
                .deadline(deadline)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
