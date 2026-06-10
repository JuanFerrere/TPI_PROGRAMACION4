package ar.edu.utn.frvm.prode.prediction.service;

import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PredictionScoringService {
	private static final int POINTS_EXACT = 3;
	private static final int POINTS_TREND = 1;
	private static final int POINTS_MISS = 0;

	private final PredictionRepository predictionRepository;

	public PredictionScoringService(PredictionRepository predictionRepository) {
		this.predictionRepository = predictionRepository;
	}

	@Transactional
	public void scoreMatchPredictions(Match match) {
		List<Prediction> predictions = predictionRepository.findByMatchIdOrderByCreatedAtAsc(match.getId());

		for (Prediction prediction : predictions) {
			applyScore(prediction, match);
		}

		predictionRepository.saveAll(predictions);
	}

	private void applyScore(Prediction prediction, Match match) {
		boolean exactHit = isExactHit(prediction, match);

		if (exactHit) {
			prediction.setPoints(POINTS_EXACT);
			prediction.setExactHit(true);
			return;
		}

		if (prediction.getPredictedTrend() == match.getResultTrend()) {
			prediction.setPoints(POINTS_TREND);
			prediction.setExactHit(false);
			return;
		}

		prediction.setPoints(POINTS_MISS);
		prediction.setExactHit(false);
	}

	private boolean isExactHit(Prediction prediction, Match match) {
		return Objects.equals(prediction.getPredictedHomeGoals(), match.getHomeGoals())
				&& Objects.equals(prediction.getPredictedAwayGoals(), match.getAwayGoals());
	}
}
