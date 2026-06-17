package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.prediction.dto.PredictionUpsertRequest;
import ar.edu.utn.frvm.prode.prediction.service.PredictionService;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentPredictionResponse;
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
 * Controller REST para pronosticos filtrados por torneo.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/predictions")
public class TournamentPredictionController {

	private final PredictionService predictionService;

	public TournamentPredictionController(PredictionService predictionService) {
		this.predictionService = predictionService;
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public List<TournamentPredictionResponse> getMyTournamentPredictions(
			@PathVariable Long tournamentId,
			@RequestParam(required = false) MatchStatus matchStatus,
			Authentication authentication
	) {
		return predictionService.getMyTournamentPredictions(tournamentId, authentication, matchStatus);
	}

	@PostMapping("/matches/{matchId}")
	@PreAuthorize("isAuthenticated()")
	public TournamentPredictionResponse upsertTournamentPrediction(
			@PathVariable Long tournamentId,
			@PathVariable Long matchId,
			@Valid @RequestBody PredictionUpsertRequest request,
			Authentication authentication
	) {
		return predictionService.upsertTournamentPrediction(tournamentId, matchId, request, authentication);
	}
}
