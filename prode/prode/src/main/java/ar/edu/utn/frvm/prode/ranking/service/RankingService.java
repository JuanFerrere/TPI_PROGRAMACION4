package ar.edu.utn.frvm.prode.ranking.service;

import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.user.entity.Role;
import ar.edu.utn.frvm.prode.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingService {
	private final PredictionRepository predictionRepository;
	private final TournamentRepository tournamentRepository;

	public RankingService(
			PredictionRepository predictionRepository,
			TournamentRepository tournamentRepository
	) {
		this.predictionRepository = predictionRepository;
		this.tournamentRepository = tournamentRepository;
	}

	@Transactional(readOnly = true)
	public List<RankingResponse> getGlobalRanking() {
		List<Prediction> predictions = predictionRepository.findAll();
		return buildRanking(predictions);
	}

	@Transactional(readOnly = true)
	public List<RankingResponse> getTournamentGlobalRanking(Long tournamentId) {
		if (!tournamentRepository.existsById(tournamentId)) {
			throw new ResourceNotFoundException("Torneo no encontrado");
		}

		List<Prediction> predictions = predictionRepository.findByMatchTournamentId(tournamentId);
		return buildRanking(predictions);
	}

	private List<RankingResponse> buildRanking(List<Prediction> predictions) {
		Map<Long, UserScoreAccumulator> accumulatorByUser = new LinkedHashMap<>();

		for (Prediction prediction : predictions) {
			User user = prediction.getUser();
			if (user.getRole() != Role.USER) {
				continue;
			}

			UserScoreAccumulator accumulator = accumulatorByUser.computeIfAbsent(
					user.getId(),
					id -> new UserScoreAccumulator(user.getId(), user.getUsername())
			);
			accumulator.add(prediction);
		}

		Comparator<UserScoreAccumulator> rankingOrder = Comparator
				.comparingInt(UserScoreAccumulator::getTotalPoints).reversed()
				.thenComparing(Comparator.comparingLong(UserScoreAccumulator::getExactHits).reversed())
				.thenComparing(
						UserScoreAccumulator::getEarliestPredictionCreatedAt,
						Comparator.nullsLast(Comparator.naturalOrder())
				)
				.thenComparing(UserScoreAccumulator::getUserId);

		List<UserScoreAccumulator> ordered = accumulatorByUser.values().stream()
				.sorted(rankingOrder)
				.toList();

		List<RankingResponse> ranking = new ArrayList<>();
		int position = 1;
		for (UserScoreAccumulator accumulator : ordered) {
			ranking.add(new RankingResponse(
					position,
					accumulator.getUserId(),
					accumulator.getUsername(),
					accumulator.getTotalPoints(),
					accumulator.getExactHits(),
					accumulator.getPredictionsCount()
			));
			position++;
		}

		return ranking;
	}

	private static final class UserScoreAccumulator {
		private final Long userId;
		private final String username;
		private int totalPoints;
		private long exactHits;
		private long predictionsCount;
		private Instant earliestPredictionCreatedAt;

		private UserScoreAccumulator(Long userId, String username) {
			this.userId = userId;
			this.username = username;
		}

		private void add(Prediction prediction) {
			this.totalPoints += prediction.getPoints();
			if (Boolean.TRUE.equals(prediction.getExactHit())) {
				this.exactHits++;
			}
			this.predictionsCount++;

			Instant predictionCreatedAt = prediction.getCreatedAt();
			if (predictionCreatedAt != null
					&& (this.earliestPredictionCreatedAt == null
					|| predictionCreatedAt.isBefore(this.earliestPredictionCreatedAt))) {
				this.earliestPredictionCreatedAt = predictionCreatedAt;
			}
		}

		private Long getUserId() {
			return userId;
		}

		private String getUsername() {
			return username;
		}

		private int getTotalPoints() {
			return totalPoints;
		}

		private long getExactHits() {
			return exactHits;
		}

		private long getPredictionsCount() {
			return predictionsCount;
		}

		private Instant getEarliestPredictionCreatedAt() {
			return earliestPredictionCreatedAt;
		}
	}
}
