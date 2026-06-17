package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchResponse;
import ar.edu.utn.frvm.prode.tournament.service.TournamentMatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para partidos dentro de torneos.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/matches")
public class TournamentMatchController {

	private final TournamentMatchService tournamentMatchService;

	public TournamentMatchController(TournamentMatchService tournamentMatchService) {
		this.tournamentMatchService = tournamentMatchService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<TournamentMatchResponse> getMatches(
			@PathVariable Long tournamentId,
			@RequestParam(required = false) Long matchDayId
	) {
		if (matchDayId == null) {
			return tournamentMatchService.findAll(tournamentId);
		}

		return tournamentMatchService.findByMatchDay(tournamentId, matchDayId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentMatchResponse createMatch(
			@PathVariable Long tournamentId,
			@Valid @RequestBody TournamentMatchCreateRequest request
	) {
		return tournamentMatchService.create(tournamentId, request);
	}

	@PostMapping("/bulk")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public List<TournamentMatchResponse> createMatchesBulk(
			@PathVariable Long tournamentId,
			@Valid @RequestBody TournamentMatchBulkCreateRequest request
	) {
		return tournamentMatchService.createBulk(tournamentId, request);
	}

	@DeleteMapping("/{matchId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteMatch(
			@PathVariable Long tournamentId,
			@PathVariable Long matchId
	) {
		tournamentMatchService.remove(tournamentId, matchId);
	}
}
