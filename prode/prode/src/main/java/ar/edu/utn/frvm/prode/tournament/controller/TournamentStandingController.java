package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.tournament.dto.TournamentStandingsResponse;
import ar.edu.utn.frvm.prode.tournament.service.TournamentStandingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para la tabla deportiva de equipos de un torneo.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/standings")
public class TournamentStandingController {

	private final TournamentStandingService tournamentStandingService;

	public TournamentStandingController(TournamentStandingService tournamentStandingService) {
		this.tournamentStandingService = tournamentStandingService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public TournamentStandingsResponse getStandings(@PathVariable Long tournamentId) {
		return tournamentStandingService.getStandings(tournamentId);
	}
}
