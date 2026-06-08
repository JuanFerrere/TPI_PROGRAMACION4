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

/**
 * Controller REST para fechas o jornadas.
 *
 * Expone endpoints bajo /api/match-days y no permite que el cliente edite el estado manualmente.
 */
@RestController // Marca la clase como controller REST que responde JSON.
@RequestMapping("/api/match-days") // Prefijo comun de endpoints de fechas.
public class MatchDayController {

	private final MatchDayService matchDayService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param matchDayService service con reglas de negocio de fechas.
	 */
	public MatchDayController(MatchDayService matchDayService) {
		this.matchDayService = matchDayService;
	}

	/**
	 * Crea una fecha nueva en estado PROGRAMADA.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param request body JSON con el nombre de la fecha.
	 * @return fecha creada como DTO.
	 */
	@PostMapping // Atiende POST /api/match-days.
	@ResponseStatus(HttpStatus.CREATED) // Devuelve 201 Created al crear.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede crear fechas.
	public MatchDayResponse createMatchDay(
			@Valid // Ejecuta validaciones de nombre obligatorio y longitud.
			@RequestBody MatchDayCreateRequest request // Convierte el JSON en DTO de entrada.
	) {
		return matchDayService.createMatchDay(request);
	}

	/**
	 * Lista fechas o las filtra por estado.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param status query param opcional: PROGRAMADA, EN_JUEGO o FINALIZADA.
	 * @return lista de fechas como DTOs.
	 */
	@GetMapping // Atiende GET /api/match-days.
	@PreAuthorize("isAuthenticated()") // Requiere JWT valido, sin exigir ADMIN.
	public List<MatchDayResponse> getMatchDays(
			@RequestParam(required = false) MatchDayStatus status // Spring convierte el texto del query param al enum.
	) {
		if (status == null) {
			return matchDayService.getAllMatchDays();
		}
		return matchDayService.getMatchDaysByStatus(status);
	}

	/**
	 * Obtiene una fecha por id.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param id identificador de la fecha en la URL.
	 * @return fecha encontrada como DTO.
	 */
	@GetMapping("/{id}") // Atiende GET /api/match-days/{id}.
	@PreAuthorize("isAuthenticated()") // Cualquier usuario autenticado puede consultar.
	public MatchDayResponse getMatchDayById(
			@PathVariable Long id // Toma el {id} de la URL.
	) {
		return matchDayService.getMatchDayById(id);
	}

	/**
	 * Modifica el nombre de una fecha si esta PROGRAMADA y sin partidos.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador de la fecha.
	 * @param request body JSON con el nuevo nombre.
	 * @return fecha actualizada como DTO.
	 */
	@PutMapping("/{id}") // Atiende PUT /api/match-days/{id}.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede modificar fechas.
	public MatchDayResponse updateMatchDay(
			@PathVariable Long id,
			@Valid // Valida que el nombre nuevo sea correcto.
			@RequestBody MatchDayUpdateRequest request
	) {
		return matchDayService.updateMatchDay(id, request);
	}

	/**
	 * Elimina una fecha si esta PROGRAMADA y sin partidos.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador de la fecha a eliminar.
	 */
	@DeleteMapping("/{id}") // Atiende DELETE /api/match-days/{id}.
	@ResponseStatus(HttpStatus.NO_CONTENT) // Devuelve 204 cuando elimina correctamente.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede eliminar fechas.
	public void deleteMatchDay(
			@PathVariable Long id
	) {
		matchDayService.deleteMatchDay(id);
	}
}
