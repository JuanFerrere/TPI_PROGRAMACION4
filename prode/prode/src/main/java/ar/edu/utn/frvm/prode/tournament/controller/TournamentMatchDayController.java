package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayResponse;
import ar.edu.utn.frvm.prode.tournament.service.TournamentMatchDayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para fechas dentro de torneos.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/match-days")
public class TournamentMatchDayController {

	private final TournamentMatchDayService tournamentMatchDayService;

	public TournamentMatchDayController(TournamentMatchDayService tournamentMatchDayService) {
		this.tournamentMatchDayService = tournamentMatchDayService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<TournamentMatchDayResponse> getMatchDays(
			@PathVariable Long tournamentId
	) {
		return tournamentMatchDayService.findAll(tournamentId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentMatchDayResponse createMatchDay(
			@PathVariable Long tournamentId,
			@Valid @RequestBody TournamentMatchDayCreateRequest request
	) {
		return tournamentMatchDayService.create(tournamentId, request);
	}

	@PostMapping("/bulk")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public List<TournamentMatchDayResponse> createMatchDaysBulk(
			@PathVariable Long tournamentId,
			@Valid @RequestBody TournamentMatchDayBulkCreateRequest request
	) {
		return tournamentMatchDayService.createBulk(tournamentId, request);
	}

	@DeleteMapping("/{matchDayId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteMatchDay(
			@PathVariable Long tournamentId,
			@PathVariable Long matchDayId
	) {
		tournamentMatchDayService.remove(tournamentId, matchDayId);
	}
}
