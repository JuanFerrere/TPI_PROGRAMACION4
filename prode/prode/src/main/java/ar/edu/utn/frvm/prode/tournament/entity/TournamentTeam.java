package ar.edu.utn.frvm.prode.tournament.entity;

import ar.edu.utn.frvm.prode.team.entity.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Participacion de un equipo dentro de un torneo.
 *
 * No reemplaza a Team global: vincula un equipo existente o nuevo al torneo y,
 * si el formato del torneo lo requiere, guarda el grupo.
 */
@Entity
@Table(
		name = "tournament_teams",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_tournament_teams_tournament_team",
				columnNames = {"tournament_id", "team_id"}
		)
)
@Getter
@Setter
@NoArgsConstructor
public class TournamentTeam {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tournament_id", nullable = false)
	private Tournament tournament;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(length = 20)
	private String groupName;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public TournamentTeam(Tournament tournament, Team team, String groupName) {
		this.tournament = tournament;
		this.team = team;
		this.groupName = groupName;
	}

	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
