package ar.edu.utn.frvm.prode.match.entity;

import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.team.entity.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_day_id", nullable = false)
	private MatchDay matchDay;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "home_team_id", nullable = false)
	private Team homeTeam;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "away_team_id", nullable = false)
	private Team awayTeam;

	@Column(nullable = false)
	private Instant startTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MatchStatus status;

	@Column
	private Integer homeGoals;

	@Column
	private Integer awayGoals;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ResultTrend resultTrend;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public Match(MatchDay matchDay, Team homeTeam, Team awayTeam, Instant startTime) {
		this.matchDay = matchDay;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.startTime = startTime;
	}

	@PrePersist
	public void prePersist() {
		if (this.status == null) {
			this.status = MatchStatus.POR_JUGARSE;
		}
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
