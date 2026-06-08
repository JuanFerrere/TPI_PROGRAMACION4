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

/**
 * Controller REST para gestionar equipos.
 *
 * Expone endpoints bajo /api/teams y delega la logica de negocio en TeamService.
 */
@RestController // Indica que esta clase recibe HTTP y responde JSON.
@RequestMapping("/api/teams") // Prefijo comun de todos los endpoints de equipos.
public class TeamController {

	private final TeamService teamService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param teamService service que contiene la logica de equipos.
	 */
	public TeamController(TeamService teamService) {
		this.teamService = teamService;
	}

	/**
	 * Crea un equipo nuevo.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param request body JSON con el nombre del equipo.
	 * @return equipo creado como DTO de salida.
	 */
	@PostMapping // Atiende POST /api/teams.
	@ResponseStatus(HttpStatus.CREATED) // Devuelve 201 Created si el equipo se crea correctamente.
	@PreAuthorize("hasRole('ADMIN')") // Solo usuarios con autoridad ROLE_ADMIN pueden administrar equipos.
	public TeamResponse createTeam(
			@Valid // Ejecuta validaciones del DTO antes de entrar al service.
			@RequestBody TeamCreateRequest request // Convierte el JSON recibido en TeamCreateRequest.
	) {
		return teamService.createTeam(request);
	}

	/**
	 * Lista equipos o los filtra por nombre parcial.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param name query param opcional; ejemplo: /api/teams?name=boca.
	 * @return lista de equipos como DTOs.
	 */
	@GetMapping // Atiende GET /api/teams.
	@PreAuthorize("isAuthenticated()") // Cualquier usuario con JWT valido puede consultar.
	public List<TeamResponse> getTeams(
			@RequestParam(required = false) String name // Si viene informado, se usa como filtro de busqueda.
	) {
		return teamService.searchTeamsByName(name);
	}

	/**
	 * Obtiene un equipo puntual por id.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param id identificador del equipo dentro de la URL.
	 * @return equipo encontrado como DTO.
	 */
	@GetMapping("/{id}") // Atiende GET /api/teams/{id}.
	@PreAuthorize("isAuthenticated()") // Requiere JWT valido, sin exigir rol ADMIN.
	public TeamResponse getTeamById(
			@PathVariable Long id // Toma el valor {id} de la URL.
	) {
		return teamService.getTeamById(id);
	}

	/**
	 * Elimina un equipo si no esta asociado a partidos.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador del equipo a eliminar.
	 */
	@DeleteMapping("/{id}") // Atiende DELETE /api/teams/{id}.
	@ResponseStatus(HttpStatus.NO_CONTENT) // Devuelve 204 No Content cuando elimina correctamente.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede eliminar equipos.
	public void deleteTeam(
			@PathVariable Long id // Toma el id desde la URL.
	) {
		teamService.deleteTeam(id);
	}
}
