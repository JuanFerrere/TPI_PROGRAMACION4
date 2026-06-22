package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.tournament.dto.KnockoutBracketResponse;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutGenerateRequest;
import ar.edu.utn.frvm.prode.tournament.service.TournamentKnockoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para la llave eliminatoria de un torneo.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/knockout")
public class TournamentKnockoutController {

	private final TournamentKnockoutService tournamentKnockoutService;

	public TournamentKnockoutController(TournamentKnockoutService tournamentKnockoutService) {
		this.tournamentKnockoutService = tournamentKnockoutService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public KnockoutBracketResponse getKnockout(
			@PathVariable Long tournamentId
	) {
		return tournamentKnockoutService.getBracket(tournamentId);
	}

	@PostMapping("/generate")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public KnockoutBracketResponse generateKnockout(
			@PathVariable Long tournamentId,
			@Valid @RequestBody KnockoutGenerateRequest request
	) {
		return tournamentKnockoutService.generate(tournamentId, request);
	}
}
