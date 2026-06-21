package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.tournament.dto.TournamentCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentFormatUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStatusUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Controller REST para gestionar torneos.
 *
 * Expone endpoints bajo /api/tournaments sin afectar los endpoints globales ya existentes.
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

	private final TournamentService tournamentService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param tournamentService service de torneos.
	 */
	public TournamentController(TournamentService tournamentService) {
		this.tournamentService = tournamentService;
	}

	/**
	 * Crea un torneo nuevo.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param request body JSON con nombre y descripcion opcional.
	 * @return torneo creado como DTO.
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentResponse createTournament(
			@Valid
			@RequestBody TournamentCreateRequest request
	) {
		return tournamentService.create(request);
	}

	/**
	 * Lista todos los torneos.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @return lista de torneos.
	 */
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<TournamentResponse> getTournaments() {
		return tournamentService.findAll();
	}

	@GetMapping("/available")
	@PreAuthorize("isAuthenticated()")
	public List<TournamentResponse> getAvailableTournaments() {
		return tournamentService.findAvailable();
	}

	/**
	 * Obtiene un torneo puntual por id.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param tournamentId identificador del torneo.
	 * @return torneo encontrado.
	 */
	@GetMapping("/{tournamentId}")
	@PreAuthorize("isAuthenticated()")
	public TournamentResponse getTournamentById(
			@PathVariable Long tournamentId
	) {
		return tournamentService.findById(tournamentId);
	}

	/**
	 * Actualiza el estado de un torneo.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param tournamentId identificador del torneo.
	 * @param request body JSON con el nuevo estado.
	 * @return torneo actualizado.
	 */
	@PatchMapping("/{tournamentId}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentResponse updateTournamentStatus(
			@PathVariable Long tournamentId,
			@Valid
			@RequestBody TournamentStatusUpdateRequest request
	) {
		return tournamentService.updateStatus(tournamentId, request);
	}

	/**
	 * Actualiza el formato de un torneo.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param tournamentId identificador del torneo.
	 * @param request body JSON con el nuevo formato.
	 * @return torneo actualizado.
	 */
	@PatchMapping("/{tournamentId}/format")
	@PreAuthorize("hasRole('ADMIN')")
	public TournamentResponse updateTournamentFormat(
			@PathVariable Long tournamentId,
			@Valid
			@RequestBody TournamentFormatUpdateRequest request
	) {
		return tournamentService.updateFormat(tournamentId, request);
	}
}
