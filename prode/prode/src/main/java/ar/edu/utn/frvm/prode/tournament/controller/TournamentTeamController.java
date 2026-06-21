package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamGroupUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamResponse;
import ar.edu.utn.frvm.prode.tournament.service.TournamentTeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para administrar equipos dentro de un torneo.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/teams")
public class TournamentTeamController {

	private final TournamentTeamService tournamentTeamService;

	public TournamentTeamController(TournamentTeamService tournamentTeamService) {
		this.tournamentTeamService = tournamentTeamService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<TournamentTeamResponse> getTournamentTeams(
			@PathVariable Long tournamentId
	) {
		return tournamentTeamService.findByTournament(tournamentId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentTeamResponse addTournamentTeam(
			@PathVariable Long tournamentId,
			@Valid
			@RequestBody TournamentTeamCreateRequest request
	) {
		return tournamentTeamService.addTeam(tournamentId, request);
	}

	@PostMapping("/bulk")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public List<TournamentTeamResponse> addTournamentTeamsBulk(
			@PathVariable Long tournamentId,
			@Valid
			@RequestBody TournamentTeamBulkCreateRequest request
	) {
		return tournamentTeamService.addTeamsBulk(tournamentId, request);
	}

	@PatchMapping("/{tournamentTeamId}/group")
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentTeamResponse updateTournamentTeamGroup(
			@PathVariable Long tournamentId,
			@PathVariable Long tournamentTeamId,
			@Valid
			@RequestBody TournamentTeamGroupUpdateRequest request
	) {
		return tournamentTeamService.updateGroup(tournamentId, tournamentTeamId, request);
	}

	@DeleteMapping("/{tournamentTeamId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void removeTournamentTeam(
			@PathVariable Long tournamentId,
			@PathVariable Long tournamentTeamId
	) {
		tournamentTeamService.removeTeam(tournamentId, tournamentTeamId);
	}
}
