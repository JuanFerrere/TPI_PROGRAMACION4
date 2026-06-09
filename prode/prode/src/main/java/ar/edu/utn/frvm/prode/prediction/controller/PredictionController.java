package ar.edu.utn.frvm.prode.prediction.controller;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.prediction.dto.PredictionResponse;
import ar.edu.utn.frvm.prode.prediction.dto.PredictionUpsertRequest;
import ar.edu.utn.frvm.prode.prediction.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para pronosticos.
 *
 * Todos los endpoints requieren usuario autenticado porque un pronostico siempre
 * pertenece a una cuenta real del sistema.
 */
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

	private final PredictionService predictionService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param predictionService service que contiene reglas de pronosticos.
	 */
	public PredictionController(PredictionService predictionService) {
		this.predictionService = predictionService;
	}

	/**
	 * Crea o modifica el pronostico del usuario autenticado para un partido.
	 *
	 * @param matchId id del partido dentro de la URL.
	 * @param request body JSON con goles pronosticados.
	 * @param authentication usuario autenticado cargado por JWT.
	 * @return pronostico creado o actualizado.
	 */
	@PostMapping("/matches/{matchId}")
	@PreAuthorize("isAuthenticated()")
	public PredictionResponse upsertPrediction(
			@PathVariable Long matchId,
			@Valid @RequestBody PredictionUpsertRequest request,
			Authentication authentication
	) {
		return predictionService.upsertPrediction(matchId, request, authentication);
	}

	/**
	 * Lista los pronosticos propios del usuario autenticado.
	 *
	 * @param matchStatus filtro opcional por estado del partido.
	 * @param authentication usuario autenticado cargado por JWT.
	 * @return lista de pronosticos propios.
	 */
	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public List<PredictionResponse> getMyPredictions(
			@RequestParam(required = false) MatchStatus matchStatus,
			Authentication authentication
	) {
		return predictionService.getMyPredictions(authentication, matchStatus);
	}

	/**
	 * Devuelve los pronosticos de un partido cuando ya cerro el periodo de carga.
	 *
	 * Antes del cierre, el service bloquea la respuesta para proteger la
	 * privacidad de los pronosticos de terceros.
	 *
	 * @param matchId id del partido consultado.
	 * @return lista de pronosticos del partido.
	 */
	@GetMapping("/matches/{matchId}")
	@PreAuthorize("isAuthenticated()")
	public List<PredictionResponse> getPredictionsByMatch(
			@PathVariable Long matchId
	) {
		return predictionService.getPredictionsByMatch(matchId);
	}
}
