package ar.edu.utn.frvm.prode.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
		name = "teams",
		uniqueConstraints = @UniqueConstraint(name = "uk_teams_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public Team(String name) {
		this.name = name;
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
