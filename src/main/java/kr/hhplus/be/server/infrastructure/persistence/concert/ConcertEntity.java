package kr.hhplus.be.server.infrastructure.persistence.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.infrastructure.persistence.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "CONCERT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class ConcertEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "id", length = 36)
	private String id;

	@Column(name = "title", length = 100, nullable = false)
	private String title;

	@Column(name = "artist", length = 50, nullable = false)
	private String artist;

	public static ConcertEntity from(Concert concert) {
		return ConcertEntity.builder()
				.id(concert.id() != null ? concert.id().toString() : null)
				.title(concert.title())
				.artist(concert.artist())
				.build();
	}

	public Concert toDomain() {
		return Concert.builder()
				.id(UUID.fromString(id))
				.title(title)
				.artist(artist)
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.build();
	}
}
