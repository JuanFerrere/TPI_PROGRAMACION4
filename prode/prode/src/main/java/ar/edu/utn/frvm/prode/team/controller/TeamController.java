package ar.edu.utn.frvm.prode.team.controller;

import ar.edu.utn.frvm.prode.team.dto.TeamCreateRequest;
import ar.edu.utn.frvm.prode.team.dto.TeamResponse;
import ar.edu.utn.frvm.prode.team.service.TeamService;
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

@RestController
@RequestMapping("/api/teams")
public class TeamController {
	private final TeamService teamService;

	public TeamController(TeamService teamService) {
		this.teamService = teamService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public TeamResponse createTeam(
			@Valid
			@RequestBody TeamCreateRequest request
	) {
		return teamService.createTeam(request);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<TeamResponse> getTeams(
			@RequestParam(required = false) String name
	) {
		return teamService.searchTeamsByName(name);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public TeamResponse getTeamById(
			@PathVariable Long id
	) {
		return teamService.getTeamById(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteTeam(
			@PathVariable Long id
	) {
		teamService.deleteTeam(id);
	}
}
