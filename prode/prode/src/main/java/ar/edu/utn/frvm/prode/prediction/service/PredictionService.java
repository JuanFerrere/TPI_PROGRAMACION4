package ar.edu.utn.frvm.prode.prediction.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.prediction.dto.PredictionResponse;
import ar.edu.utn.frvm.prode.prediction.dto.PredictionUpsertRequest;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PredictionService {
	private static final long PREDICTION_LOCK_MINUTES = 30;

	private final PredictionRepository predictionRepository;
	private final UserRepository userRepository;
	private final MatchRepository matchRepository;

	public PredictionService(
			PredictionRepository predictionRepository,
			UserRepository userRepository,
			MatchRepository matchRepository
	) {
		this.predictionRepository = predictionRepository;
		this.userRepository = userRepository;
		this.matchRepository = matchRepository;
	}

	@Transactional
	public PredictionResponse upsertPrediction(
			Long matchId,
			PredictionUpsertRequest request,
			Authentication authentication
	) {
		User user = getAuthenticatedUser(authentication);
		Match match = getMatchById(matchId);

		validateMatchCanBePredicted(match);
		validatePredictionWindow(match);

		ResultTrend predictedTrend = calculateTrend(
				request.predictedHomeGoals(),
				request.predictedAwayGoals()
		);

		Prediction prediction = predictionRepository.findByUserIdAndMatchId(user.getId(), match.getId())
				.orElseGet(() -> new Prediction(user, match));

		prediction.updatePrediction(
				request.predictedHomeGoals(),
				request.predictedAwayGoals(),
				predictedTrend
		);

		Prediction savedPrediction = predictionRepository.save(prediction);
		return toResponse(savedPrediction);
	}

	@Transactional(readOnly = true)
	public List<PredictionResponse> getMyPredictions(
			Authentication authentication,
			MatchStatus optionalStatus
	) {
		User user = getAuthenticatedUser(authentication);

		List<Prediction> predictions;
		if (optionalStatus == null) {
			predictions = predictionRepository.findByUserIdOrderByMatchStartTimeAsc(user.getId());
		} else {
			predictions = predictionRepository.findByUserIdAndMatchStatusOrderByMatchStartTimeAsc(
					user.getId(),
					optionalStatus
			);
		}

		return predictions.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<PredictionResponse> getPredictionsByMatch(Long matchId) {
		Match match = getMatchById(matchId);
		validatePredictionsAreVisible(match);

		return predictionRepository.findByMatchIdOrderByCreatedAtAsc(matchId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private void validateMatchCanBePredicted(Match match) {
		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("Solo se pueden pronosticar partidos que estan POR_JUGARSE");
		}
	}

	private void validatePredictionWindow(Match match) {
		Instant lockTime = getPredictionLockTime(match);
		Instant serverNow = Instant.now();

		if (!serverNow.isBefore(lockTime)) {
			throw new BusinessRuleException("No se puede crear o modificar el pronostico porque el periodo de carga ya esta bloqueado");
		}
	}

	private void validatePredictionsAreVisible(Match match) {
		Instant lockTime = getPredictionLockTime(match);
		Instant serverNow = Instant.now();

		if (serverNow.isBefore(lockTime)) {
			throw new BusinessRuleException("Los pronosticos de otros usuarios estaran disponibles cuando cierre el periodo de carga");
		}
	}

	private Instant getPredictionLockTime(Match match) {
		return match.getStartTime().minus(PREDICTION_LOCK_MINUTES, ChronoUnit.MINUTES);
	}

	private ResultTrend calculateTrend(int homeGoals, int awayGoals) {
		if (homeGoals > awayGoals) {
			return ResultTrend.LOCAL;
		}

		if (homeGoals < awayGoals) {
			return ResultTrend.VISITANTE;
		}

		return ResultTrend.EMPATE;
	}

	private User getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Usuario no autenticado");
		}

		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
	}

	private Match getMatchById(Long matchId) {
		return matchRepository.findById(matchId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
	}

	private PredictionResponse toResponse(Prediction prediction) {
		Match match = prediction.getMatch();

		return new PredictionResponse(
				prediction.getId(),
				prediction.getUser().getId(),
				prediction.getUser().getUsername(),
				match.getId(),
				match.getMatchDay().getName(),
				match.getHomeTeam().getName(),
				match.getAwayTeam().getName(),
				match.getStartTime(),
				match.getStatus(),
				prediction.getPredictedHomeGoals(),
				prediction.getPredictedAwayGoals(),
				prediction.getPredictedTrend(),
				prediction.getPoints(),
				prediction.getExactHit(),
				prediction.getCreatedAt(),
				prediction.getUpdatedAt()
		);
	}
}
