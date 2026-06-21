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
import ar.edu.utn.frvm.prode.tournament.dto.TournamentPredictionResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service de pronosticos.
 *
 * Centraliza las reglas de negocio de Etapa 4 para que el controller solo
 * reciba HTTP y delegue decisiones importantes.
 */
@Service
public class PredictionService {

	private static final long PREDICTION_LOCK_MINUTES = 30;

	private final PredictionRepository predictionRepository;
	private final UserRepository userRepository;
	private final MatchRepository matchRepository;
	private final TournamentRepository tournamentRepository;

	/**
	 * Constructor con repositorios necesarios.
	 *
	 * @param predictionRepository repositorio para consultar y guardar pronosticos.
	 * @param userRepository repositorio para resolver el usuario autenticado.
	 * @param matchRepository repositorio para validar partidos existentes.
	 */
	public PredictionService(
			PredictionRepository predictionRepository,
			UserRepository userRepository,
			MatchRepository matchRepository,
			TournamentRepository tournamentRepository
	) {
		this.predictionRepository = predictionRepository;
		this.userRepository = userRepository;
		this.matchRepository = matchRepository;
		this.tournamentRepository = tournamentRepository;
	}

	/**
	 * Crea o modifica el pronostico del usuario autenticado para un partido.
	 *
	 * Upsert significa: si el pronostico ya existe, se actualiza; si no existe,
	 * se crea. Esto evita duplicados y respeta la restriccion unica user + match.
	 *
	 * @param matchId id del partido a pronosticar.
	 * @param request goles pronosticados por el cliente.
	 * @param authentication usuario autenticado que llega desde Spring Security.
	 * @return pronostico creado o actualizado.
	 */
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

	@Transactional
	public TournamentPredictionResponse upsertTournamentPrediction(
			Long tournamentId,
			Long matchId,
			PredictionUpsertRequest request,
			Authentication authentication
	) {
		User user = getAuthenticatedUser(authentication);
		Tournament tournament = getTournamentById(tournamentId);
		validateTournamentAllowsPredictions(tournament);
		Match match = getMatchByIdAndTournamentId(matchId, tournamentId);

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
		return toTournamentResponse(savedPrediction, tournament);
	}

	/**
	 * Lista los pronosticos del usuario autenticado.
	 *
	 * @param authentication usuario autenticado.
	 * @param optionalStatus filtro opcional por estado del partido.
	 * @return lista de pronosticos propios.
	 */
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
	public List<TournamentPredictionResponse> getMyTournamentPredictions(
			Long tournamentId,
			Authentication authentication,
			MatchStatus optionalStatus
	) {
		User user = getAuthenticatedUser(authentication);
		Tournament tournament = getTournamentById(tournamentId);

		List<Prediction> predictions;
		if (optionalStatus == null) {
			predictions = predictionRepository.findByUserIdAndMatchTournamentIdOrderByMatchStartTimeAsc(
					user.getId(),
					tournamentId
			);
		} else {
			predictions = predictionRepository.findByUserIdAndMatchTournamentIdAndMatchStatusOrderByMatchStartTimeAsc(
					user.getId(),
					tournamentId,
					optionalStatus
			);
		}

		return predictions.stream()
				.map(prediction -> toTournamentResponse(prediction, tournament))
				.toList();
	}

	/**
	 * Devuelve los pronosticos de un partido.
	 *
	 * Regla de privacidad: los pronosticos de otros usuarios solo se pueden ver
	 * cuando ya cerro el periodo de carga. Antes de ese cierre, revelar
	 * pronosticos ajenos afectaria la integridad del Prode.
	 *
	 * @param matchId id del partido.
	 * @return lista de pronosticos del partido si ya cerro la carga.
	 */
	@Transactional(readOnly = true)
	public List<PredictionResponse> getPredictionsByMatch(Long matchId) {
		Match match = getMatchById(matchId);
		validatePredictionsAreVisible(match);

		return predictionRepository.findByMatchIdOrderByCreatedAtAsc(matchId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Valida que el partido todavia pueda recibir pronosticos.
	 *
	 * Solo se puede pronosticar un partido que todavia esta POR_JUGARSE. Si ya
	 * esta EN_JUEGO o FINALIZADO, el pronostico llegaria tarde.
	 *
	 * @param match partido a validar.
	 */
	private void validateMatchCanBePredicted(Match match) {
		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("Solo se pueden pronosticar partidos que estan POR_JUGARSE");
		}
	}

	/**
	 * Valida la ventana de carga de pronosticos.
	 *
	 * La hora actual se toma del servidor con Instant.now(). No se confia en una
	 * fecha enviada por el cliente, porque el cliente podria manipularla. La hora
	 * limite es startTime - 30 minutos. Si la hora actual es igual o posterior a
	 * esa hora limite, se rechaza la creacion o modificacion.
	 *
	 * @param match partido a validar.
	 */
	private void validatePredictionWindow(Match match) {
		Instant lockTime = getPredictionLockTime(match);
		Instant serverNow = Instant.now();

		// Regla critica: desde 30 minutos antes del partido ya no se puede modificar el pronostico.
		if (!serverNow.isBefore(lockTime)) {
			throw new BusinessRuleException("No se puede crear o modificar el pronostico porque el periodo de carga ya esta bloqueado");
		}
	}

	/**
	 * Valida si los pronosticos de otros usuarios ya pueden mostrarse.
	 *
	 * Se permite verlos cuando la hora actual del servidor es igual o posterior
	 * al cierre de carga. Antes de eso, se protegen para evitar ventaja deportiva.
	 *
	 * @param match partido consultado.
	 */
	private void validatePredictionsAreVisible(Match match) {
		Instant lockTime = getPredictionLockTime(match);
		Instant serverNow = Instant.now();

		if (serverNow.isBefore(lockTime)) {
			throw new BusinessRuleException("Los pronosticos de otros usuarios estaran disponibles cuando cierre el periodo de carga");
		}
	}

	/**
	 * Calcula la hora de cierre de carga de pronosticos.
	 *
	 * @param match partido consultado.
	 * @return instante UTC que representa startTime menos 30 minutos.
	 */
	private Instant getPredictionLockTime(Match match) {
		return match.getStartTime().minus(PREDICTION_LOCK_MINUTES, ChronoUnit.MINUTES);
	}

	/**
	 * Calcula la tendencia del pronostico a partir de los goles cargados.
	 *
	 * @param homeGoals goles pronosticados del local.
	 * @param awayGoals goles pronosticados del visitante.
	 * @return LOCAL si gana local, VISITANTE si gana visitante o EMPATE si son iguales.
	 */
	private ResultTrend calculateTrend(int homeGoals, int awayGoals) {
		if (homeGoals > awayGoals) {
			return ResultTrend.LOCAL;
		}

		if (homeGoals < awayGoals) {
			return ResultTrend.VISITANTE;
		}

		return ResultTrend.EMPATE;
	}

	/**
	 * Obtiene el usuario autenticado a partir de Spring Security.
	 *
	 * El filtro JWT deja cargado el username en Authentication. Con ese username
	 * se consulta la base para obtener la entidad User real.
	 *
	 * @param authentication datos de autenticacion de la request.
	 * @return usuario autenticado.
	 */
	private User getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Usuario no autenticado");
		}

		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
	}

	/**
	 * Busca un partido por id.
	 *
	 * @param matchId id del partido.
	 * @return partido encontrado.
	 */
	private Match getMatchById(Long matchId) {
		return matchRepository.findById(matchId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
	}

	private Match getMatchByIdAndTournamentId(Long matchId, Long tournamentId) {
		return matchRepository.findByIdAndTournamentId(matchId, tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido del torneo no encontrado"));
	}

	private Tournament getTournamentById(Long tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
	}

	private void validateTournamentAllowsPredictions(Tournament tournament) {
		if (tournament.getStatus() != TournamentStatus.ACTIVE) {
			throw new BusinessRuleException("Solo se pueden cargar pronosticos en torneos ACTIVE");
		}
	}

	/**
	 * Convierte una entidad Prediction en un DTO seguro para responder.
	 *
	 * No expone password ni datos sensibles del usuario.
	 *
	 * @param prediction entidad JPA interna.
	 * @return DTO de salida.
	 */
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

	private TournamentPredictionResponse toTournamentResponse(Prediction prediction, Tournament tournament) {
		Match match = prediction.getMatch();

		return new TournamentPredictionResponse(
				prediction.getId(),
				tournament.getId(),
				tournament.getName(),
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
				match.getHomeGoals(),
				match.getAwayGoals(),
				match.getResultTrend(),
				prediction.getPoints(),
				prediction.getExactHit(),
				prediction.getCreatedAt(),
				prediction.getUpdatedAt()
		);
	}
}
