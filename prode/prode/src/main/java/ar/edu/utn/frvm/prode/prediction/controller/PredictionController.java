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

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
	private final PredictionService predictionService;

	public PredictionController(PredictionService predictionService) {
		this.predictionService = predictionService;
	}

	@PostMapping("/matches/{matchId}")
	@PreAuthorize("isAuthenticated()")
	public PredictionResponse upsertPrediction(
			@PathVariable Long matchId,
			@Valid @RequestBody PredictionUpsertRequest request,
			Authentication authentication
	) {
		return predictionService.upsertPrediction(matchId, request, authentication);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public List<PredictionResponse> getMyPredictions(
			@RequestParam(required = false) MatchStatus matchStatus,
			Authentication authentication
	) {
		return predictionService.getMyPredictions(authentication, matchStatus);
	}

	@GetMapping("/matches/{matchId}")
	@PreAuthorize("isAuthenticated()")
	public List<PredictionResponse> getPredictionsByMatch(
			@PathVariable Long matchId
	) {
		return predictionService.getPredictionsByMatch(matchId);
	}
}
