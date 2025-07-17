package kr.hhplus.be.server.concert.adapter.out.persistence.soldOutRank;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.server.concert.domain.SoldOutRank;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SOLD_OUT_RANK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class SoldOutRankEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private String id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "concert_id", length = 36, nullable = false)
    private String concertId;

    @Column(name = "score", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long score;

    @Column(name = "sold_out_time", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private long soldOutTime;

    public static SoldOutRankEntity from(SoldOutRank soldOutRank) {
        return SoldOutRankEntity.builder()
                .id(soldOutRank.id() != null ? soldOutRank.id().toString() : null)
                .concertId(soldOutRank.concertId().toString())
                .score(soldOutRank.score())
                .soldOutTime(soldOutRank.soldOutTime())
                .build();
    }

    public SoldOutRank toDomain() {
        return SoldOutRank.builder()
                .id(UUID.fromString(id))
                .concertId(UUID.fromString(concertId))
                .score(score)
                .soldOutTime(soldOutTime)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
