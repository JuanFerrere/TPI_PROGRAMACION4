package ar.edu.utn.frvm.prode.matchday.controller;

import ar.edu.utn.frvm.prode.matchday.dto.MatchDayCreateRequest;
import ar.edu.utn.frvm.prode.matchday.dto.MatchDayResponse;
import ar.edu.utn.frvm.prode.matchday.dto.MatchDayUpdateRequest;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import ar.edu.utn.frvm.prode.matchday.service.MatchDayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/match-days")
public class MatchDayController {
	private final MatchDayService matchDayService;

	public MatchDayController(MatchDayService matchDayService) {
		this.matchDayService = matchDayService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public MatchDayResponse createMatchDay(
			@Valid
			@RequestBody MatchDayCreateRequest request
	) {
		return matchDayService.createMatchDay(request);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<MatchDayResponse> getMatchDays(
			@RequestParam(required = false) MatchDayStatus status
	) {
		if (status == null) {
			return matchDayService.getAllMatchDays();
		}
		return matchDayService.getMatchDaysByStatus(status);
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public MatchDayResponse getMatchDayById(
			@PathVariable Long id
	) {
		return matchDayService.getMatchDayById(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public MatchDayResponse updateMatchDay(
			@PathVariable Long id,
			@Valid
			@RequestBody MatchDayUpdateRequest request
	) {
		return matchDayService.updateMatchDay(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteMatchDay(
			@PathVariable Long id
	) {
		matchDayService.deleteMatchDay(id);
	}
}
