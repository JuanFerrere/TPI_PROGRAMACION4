package ar.edu.utn.frvm.prode.matchday.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
		name = "match_days",
		uniqueConstraints = @UniqueConstraint(name = "uk_match_days_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class MatchDay {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MatchDayStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public MatchDay(String name) {
		this.name = name;
	}

	@PrePersist
	public void prePersist() {
		if (this.status == null) {
			this.status = MatchDayStatus.PROGRAMADA;
		}
		this.createdAt = Instant.now();
	}
}
