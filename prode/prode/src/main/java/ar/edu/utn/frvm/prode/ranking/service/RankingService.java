package ar.edu.utn.frvm.prode.ranking.service;

import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service del ranking global.
 *
 * Construye la tabla de posiciones sumando los puntos que cada usuario obtuvo en
 * sus pronosticos. El calculo se hace en memoria dentro del service, que para el
 * tamano de este TPI es claro y suficiente.
 *
 * Nota de rendimiento: recorrer todos los pronosticos y acceder al usuario de
 * cada uno puede generar consultas extra por la relacion LAZY (problema N+1).
 * Para un proyecto mas grande se podria reemplazar por una consulta agregada con
 * @Query (GROUP BY user). Se elige la version clara en service por legibilidad.
 */
@Service // Spring registra esta clase como service de dominio.
public class RankingService {

	private final PredictionRepository predictionRepository;
	private final TournamentRepository tournamentRepository;

	/**
	 * Constructor con el repositorio de pronosticos.
	 *
	 * @param predictionRepository repositorio usado para leer todos los pronosticos.
	 */
	public RankingService(
			PredictionRepository predictionRepository,
			TournamentRepository tournamentRepository
	) {
		this.predictionRepository = predictionRepository;
		this.tournamentRepository = tournamentRepository;
	}

	/**
	 * Calcula el ranking global ordenado.
	 *
	 * Pasos:
	 * 1. Trae todos los pronosticos.
	 * 2. Los agrupa por usuario acumulando puntos, aciertos exactos y cantidad.
	 * 3. Ordena por puntos (desc), luego aciertos exactos (desc) y luego username (asc).
	 * 4. Asigna posiciones secuenciales empezando en 1.
	 *
	 * Si no hay pronosticos, devuelve una lista vacia (nunca un error).
	 *
	 * @return lista ordenada de filas del ranking.
	 */
	@Transactional(readOnly = true) // Solo lee datos; no modifica nada en la base.
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
		// Se usa LinkedHashMap para acumular por usuario manteniendo un orden de insercion estable.
		Map<Long, UserScoreAccumulator> accumulatorByUser = new LinkedHashMap<>();

		for (Prediction prediction : predictions) {
			User user = prediction.getUser();
			UserScoreAccumulator accumulator = accumulatorByUser.computeIfAbsent(
					user.getId(),
					id -> new UserScoreAccumulator(user.getId(), user.getUsername())
			);
			accumulator.add(prediction);
		}

		// Reglas de desempate del ranking, en orden de prioridad.
		Comparator<UserScoreAccumulator> rankingOrder = Comparator
				.comparingInt(UserScoreAccumulator::getTotalPoints).reversed()                      // 1) mas puntos primero.
				.thenComparing(Comparator.comparingLong(UserScoreAccumulator::getExactHits).reversed()) // 2) mas aciertos exactos primero.
				.thenComparing(UserScoreAccumulator::getUsername);                                  // 3) username alfabetico como desempate estable.

		List<UserScoreAccumulator> ordered = accumulatorByUser.values().stream()
				.sorted(rankingOrder)
				.toList();

		// Se asignan posiciones secuenciales (1, 2, 3, ...) sobre la lista ya ordenada.
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

	/**
	 * Acumulador interno para sumar las estadisticas de un usuario mientras se
	 * recorren sus pronosticos.
	 *
	 * Es una clase auxiliar privada porque solo tiene sentido dentro del calculo
	 * del ranking; no es parte del modelo de dominio.
	 */
	private static final class UserScoreAccumulator {

		private final Long userId;
		private final String username;
		private int totalPoints;
		private long exactHits;
		private long predictionsCount;

		private UserScoreAccumulator(Long userId, String username) {
			this.userId = userId;
			this.username = username;
		}

		/**
		 * Suma al acumulador los datos de un pronostico del usuario.
		 *
		 * @param prediction pronostico a contabilizar.
		 */
		private void add(Prediction prediction) {
			this.totalPoints += prediction.getPoints();
			if (Boolean.TRUE.equals(prediction.getExactHit())) {
				this.exactHits++;
			}
			this.predictionsCount++;
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
	}
}
