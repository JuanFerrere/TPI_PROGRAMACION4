package ar.edu.utn.frvm.prode.prediction.service;

import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PredictionScoringServiceTest {
	private final PredictionRepository predictionRepository = mock(PredictionRepository.class);
	private final PredictionScoringService scoringService = new PredictionScoringService(predictionRepository);

	@Test
	void aciertoExactoSuma3PuntosYMarcaExactHit() {
		Match match = finishedMatch(2, 1, ResultTrend.LOCAL);
		Prediction prediction = prediction(2, 1, ResultTrend.LOCAL);
		List<Prediction> predictions = List.of(prediction);
		when(predictionRepository.findByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(predictions);

		scoringService.scoreMatchPredictions(match);

		assertEquals(3, prediction.getPoints());
		assertTrue(prediction.getExactHit());
		verify(predictionRepository).saveAll(predictions);
	}

	@Test
	void aciertoDeTendenciaSinExactoSuma1Punto() {
		Match match = finishedMatch(2, 1, ResultTrend.LOCAL);
		Prediction prediction = prediction(1, 0, ResultTrend.LOCAL);
		when(predictionRepository.findByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(prediction));

		scoringService.scoreMatchPredictions(match);

		assertEquals(1, prediction.getPoints());
		assertFalse(prediction.getExactHit());
	}

	@Test
	void falloDeTendenciaSuma0Puntos() {
		Match match = finishedMatch(2, 1, ResultTrend.LOCAL);
		Prediction prediction = prediction(0, 1, ResultTrend.VISITANTE);
		when(predictionRepository.findByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(prediction));

		scoringService.scoreMatchPredictions(match);

		assertEquals(0, prediction.getPoints());
		assertFalse(prediction.getExactHit());
	}

	@Test
	void empateExactoSuma3Puntos() {
		Match match = finishedMatch(1, 1, ResultTrend.EMPATE);
		Prediction prediction = prediction(1, 1, ResultTrend.EMPATE);
		when(predictionRepository.findByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(prediction));

		scoringService.scoreMatchPredictions(match);

		assertEquals(3, prediction.getPoints());
		assertTrue(prediction.getExactHit());
	}

	@Test
	void empateAcertadoSinMarcadorExactoSuma1Punto() {
		Match match = finishedMatch(1, 1, ResultTrend.EMPATE);
		Prediction prediction = prediction(0, 0, ResultTrend.EMPATE);
		when(predictionRepository.findByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(prediction));

		scoringService.scoreMatchPredictions(match);

		assertEquals(1, prediction.getPoints());
		assertFalse(prediction.getExactHit());
	}

	@Test
	void puntuaTodosLosPronosticosYGuardaUnaSolaVez() {
		Match match = finishedMatch(2, 1, ResultTrend.LOCAL);
		Prediction exacto = prediction(2, 1, ResultTrend.LOCAL);
		Prediction tendencia = prediction(3, 0, ResultTrend.LOCAL);
		Prediction fallo = prediction(0, 2, ResultTrend.VISITANTE);
		List<Prediction> predictions = List.of(exacto, tendencia, fallo);
		when(predictionRepository.findByMatchIdOrderByCreatedAtAsc(1L)).thenReturn(predictions);

		scoringService.scoreMatchPredictions(match);

		assertEquals(3, exacto.getPoints());
		assertEquals(1, tendencia.getPoints());
		assertEquals(0, fallo.getPoints());
		verify(predictionRepository, times(1)).saveAll(predictions);
	}

	private Match finishedMatch(int homeGoals, int awayGoals, ResultTrend trend) {
		Match match = new Match();
		match.setId(1L);
		match.setHomeGoals(homeGoals);
		match.setAwayGoals(awayGoals);
		match.setResultTrend(trend);
		return match;
	}

	private Prediction prediction(int homeGoals, int awayGoals, ResultTrend predictedTrend) {
		Prediction prediction = new Prediction();
		prediction.setPredictedHomeGoals(homeGoals);
		prediction.setPredictedAwayGoals(awayGoals);
		prediction.setPredictedTrend(predictedTrend);
		return prediction;
	}
}
