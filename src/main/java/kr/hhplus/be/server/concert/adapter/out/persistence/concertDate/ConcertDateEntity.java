package kr.hhplus.be.server.concert.adapter.out.persistence.concertDate;

import jakarta.persistence.*;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CONCERT_DATE")
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

    @Column(name = "available_seat_count")
    private Long availableSeatCount; // 예약가능 좌석 수

    @Version // 낙관적 락을 위한 버전 필드
    private Long version;

    public static ConcertDateEntity from(ConcertDate concertDate) {
        return ConcertDateEntity.builder()
                .id(concertDate.id() != null ? concertDate.id().toString() : null)
                .concertId(concertDate.concertId().toString())
                .date(concertDate.date())
                .deadline(concertDate.deadline())
                .availableSeatCount(concertDate.availableSeatCount())
                .version(concertDate.version())
                .build();
    }

    public ConcertDate toDomain() {
        return ConcertDate.builder()
                .id(UUID.fromString(id))
                .concertId(UUID.fromString(concertId))
                .date(date)
                .deadline(deadline)
                .availableSeatCount(availableSeatCount)
                .version(version)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
