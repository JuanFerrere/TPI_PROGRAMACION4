package ar.edu.utn.frvm.prode.match.controller;

import ar.edu.utn.frvm.prode.match.dto.MatchCreateRequest;
import ar.edu.utn.frvm.prode.match.dto.MatchResponse;
import ar.edu.utn.frvm.prode.match.dto.MatchResultRequest;
import ar.edu.utn.frvm.prode.match.dto.MatchUpdateRequest;
import ar.edu.utn.frvm.prode.match.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
	private final MatchService matchService;

	public MatchController(MatchService matchService) {
		this.matchService = matchService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public MatchResponse createMatch(
			@Valid
			@RequestBody MatchCreateRequest request
	) {
		return matchService.createMatch(request);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<MatchResponse> getMatches(
			@RequestParam(required = false) Long matchDayId
	) {
		if (matchDayId == null) {
			return matchService.getAllMatches();
		}
		return matchService.getMatchesByMatchDay(matchDayId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public MatchResponse getMatchById(
			@PathVariable Long id
	) {
		return matchService.getMatchById(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public MatchResponse updateMatch(
			@PathVariable Long id,
			@Valid
			@RequestBody MatchUpdateRequest request
	) {
		return matchService.updateMatch(id, request);
	}

	@PatchMapping("/{id}/start")
	@PreAuthorize("hasRole('ADMIN')")
	public MatchResponse startMatch(
			@PathVariable Long id
	) {
		return matchService.startMatch(id);
	}

	@PatchMapping("/{id}/result")
	@PreAuthorize("hasRole('ADMIN')")
	public MatchResponse loadResult(
			@PathVariable Long id,
			@Valid
			@RequestBody MatchResultRequest request
	) {
		return matchService.loadResult(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteMatch(
			@PathVariable Long id
	) {
		matchService.deleteMatch(id);
	}
}
