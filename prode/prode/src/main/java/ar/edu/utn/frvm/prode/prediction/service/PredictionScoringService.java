package ar.edu.utn.frvm.prode.prediction.service;

import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Service de puntuacion de pronosticos: el "corazon" del Prode.
 *
 * Cuando un partido se finaliza y se carga su resultado real, esta clase recorre
 * todos los pronosticos de ese partido y asigna los puntos correspondientes
 * comparando lo que el usuario pronostico contra lo que realmente paso.
 *
 * Regla de puntuacion del TPI:
 * - 3 puntos si acierta el resultado EXACTO (mismos goles de local y visitante).
 * - 1 punto si no acierta el exacto pero si la TENDENCIA (LOCAL, EMPATE o VISITANTE).
 * - 0 puntos si no acierta ni el exacto ni la tendencia.
 *
 * Se separa en su propia clase para dejar bien aislada esta logica sensible y
 * para que MatchService solo la invoque sin mezclar responsabilidades.
 */
@Service // Spring registra esta clase como service de dominio reutilizable.
public class PredictionScoringService {

	// Puntajes definidos por el reglamento del Prode. Como constantes quedan documentados y faciles de mantener.
	private static final int POINTS_EXACT = 3;
	private static final int POINTS_TREND = 1;
	private static final int POINTS_MISS = 0;

	private final PredictionRepository predictionRepository;

	/**
	 * Constructor con el repositorio de pronosticos.
	 *
	 * @param predictionRepository repositorio usado para leer y guardar los pronosticos del partido.
	 */
	public PredictionScoringService(PredictionRepository predictionRepository) {
		this.predictionRepository = predictionRepository;
	}

	/**
	 * Calcula y guarda los puntos de todos los pronosticos de un partido finalizado.
	 *
	 * Se asume que el partido ya tiene cargados homeGoals, awayGoals y resultTrend
	 * (eso lo hace MatchService antes de llamar a este metodo).
	 *
	 * @param match partido finalizado con su resultado real ya cargado.
	 */
	@Transactional // Forma parte de la transaccion de carga de resultado; los cambios se persisten juntos.
	public void scoreMatchPredictions(Match match) {
		// Se reutiliza el metodo existente del repositorio que ya trae los pronosticos del partido ordenados.
		List<Prediction> predictions = predictionRepository.findByMatchIdOrderByCreatedAtAsc(match.getId());

		for (Prediction prediction : predictions) {
			applyScore(prediction, match);
		}

		// saveAll persiste todos los cambios de puntos y aciertos en una sola operacion.
		predictionRepository.saveAll(predictions);
	}

	/**
	 * Aplica la regla de puntuacion a un pronostico individual.
	 *
	 * Compara el pronostico del usuario contra el resultado real del partido y
	 * actualiza points y exactHit segun corresponda.
	 *
	 * @param prediction pronostico del usuario a evaluar.
	 * @param match partido con el resultado real ya cargado.
	 */
	private void applyScore(Prediction prediction, Match match) {
		boolean exactHit = isExactHit(prediction, match);

		if (exactHit) {
			// Acierto exacto: mismos goles de local y visitante que el resultado real.
			prediction.setPoints(POINTS_EXACT);
			prediction.setExactHit(true);
			return;
		}

		// Si no fue exacto, todavia puede sumar 1 punto si acerto la tendencia (quien gano o empate).
		if (prediction.getPredictedTrend() == match.getResultTrend()) {
			prediction.setPoints(POINTS_TREND);
			prediction.setExactHit(false);
			return;
		}

		// No acerto ni el resultado exacto ni la tendencia: cero puntos.
		prediction.setPoints(POINTS_MISS);
		prediction.setExactHit(false);
	}

	/**
	 * Determina si un pronostico acerto el resultado exacto.
	 *
	 * Se usa Objects.equals para comparar de forma segura los Integer, evitando
	 * problemas de comparacion por referencia con ==.
	 *
	 * @param prediction pronostico del usuario.
	 * @param match partido con el resultado real.
	 * @return true si los goles pronosticados coinciden exactamente con los reales.
	 */
	private boolean isExactHit(Prediction prediction, Match match) {
		return Objects.equals(prediction.getPredictedHomeGoals(), match.getHomeGoals())
				&& Objects.equals(prediction.getPredictedAwayGoals(), match.getAwayGoals());
	}
}
