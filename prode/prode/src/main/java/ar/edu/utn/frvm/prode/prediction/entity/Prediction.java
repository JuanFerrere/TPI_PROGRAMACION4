package ar.edu.utn.frvm.prode.prediction.entity;

import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.user.entity.User;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
		name = "predictions",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_predictions_user_match",
				columnNames = {"user_id", "match_id"}
		)
)
@Getter
@Setter
@NoArgsConstructor
public class Prediction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@Column(nullable = false)
	private Integer predictedHomeGoals;

	@Column(nullable = false)
	private Integer predictedAwayGoals;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ResultTrend predictedTrend;

	@Column(nullable = false)
	private Integer points;

	@Column(nullable = false)
	private Boolean exactHit;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public Prediction(User user, Match match) {
		this.user = user;
		this.match = match;
	}

	public void updatePrediction(
			Integer predictedHomeGoals,
			Integer predictedAwayGoals,
			ResultTrend predictedTrend
	) {
		this.predictedHomeGoals = predictedHomeGoals;
		this.predictedAwayGoals = predictedAwayGoals;
		this.predictedTrend = predictedTrend;
	}

	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;

		if (this.points == null) {
			this.points = 0;
		}

		if (this.exactHit == null) {
			this.exactHit = false;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
