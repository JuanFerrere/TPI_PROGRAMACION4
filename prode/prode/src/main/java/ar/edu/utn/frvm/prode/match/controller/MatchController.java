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

/**
 * Controller REST para partidos.
 *
 * Expone endpoints bajo /api/matches y delega las reglas de negocio en MatchService.
 */
@RestController // Indica que la clase recibe HTTP y responde JSON.
@RequestMapping("/api/matches") // Prefijo comun para endpoints de partidos.
public class MatchController {

	private final MatchService matchService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param matchService service con logica de partidos.
	 */
	public MatchController(MatchService matchService) {
		this.matchService = matchService;
	}

	/**
	 * Crea un partido nuevo en estado POR_JUGARSE.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param request body JSON con fecha, local, visitante y startTime.
	 * @return partido creado como DTO.
	 */
	@PostMapping // Atiende POST /api/matches.
	@ResponseStatus(HttpStatus.CREATED) // Devuelve 201 Created al crear.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede crear partidos.
	public MatchResponse createMatch(
			@Valid // Valida ids obligatorios y startTime obligatorio.
			@RequestBody MatchCreateRequest request // Convierte el JSON a DTO de entrada.
	) {
		return matchService.createMatch(request);
	}

	/**
	 * Lista partidos ordenados por startTime ascendente.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param matchDayId query param opcional; si viene, filtra por fecha.
	 * @return lista de partidos como DTOs.
	 */
	@GetMapping // Atiende GET /api/matches.
	@PreAuthorize("isAuthenticated()") // Requiere JWT valido, sin exigir ADMIN.
	public List<MatchResponse> getMatches(
			@RequestParam(required = false) Long matchDayId // Ejemplo: /api/matches?matchDayId=1.
	) {
		if (matchDayId == null) {
			return matchService.getAllMatches();
		}
		return matchService.getMatchesByMatchDay(matchDayId);
	}

	/**
	 * Obtiene un partido por id.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param id identificador del partido.
	 * @return partido encontrado como DTO.
	 */
	@GetMapping("/{id}") // Atiende GET /api/matches/{id}.
	@PreAuthorize("isAuthenticated()") // Cualquier usuario con JWT valido puede consultar.
	public MatchResponse getMatchById(
			@PathVariable Long id // Toma el id desde la URL.
	) {
		return matchService.getMatchById(id);
	}

	/**
	 * Modifica un partido si todavia esta POR_JUGARSE.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador del partido.
	 * @param request body JSON con nuevo local, visitante y horario.
	 * @return partido actualizado como DTO.
	 */
	@PutMapping("/{id}") // Atiende PUT /api/matches/{id}.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede modificar partidos.
	public MatchResponse updateMatch(
			@PathVariable Long id,
			@Valid // Valida que ids y horario esten presentes.
			@RequestBody MatchUpdateRequest request
	) {
		return matchService.updateMatch(id, request);
	}

	/**
	 * Inicia un partido cambiando su estado a EN_JUEGO.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador del partido.
	 * @return partido iniciado como DTO.
	 */
	@PatchMapping("/{id}/start") // Atiende PATCH /api/matches/{id}/start.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede iniciar partidos.
	public MatchResponse startMatch(
			@PathVariable Long id
	) {
		return matchService.startMatch(id);
	}

	/**
	 * Carga el resultado real de un partido EN_JUEGO y lo finaliza.
	 *
	 * Este endpoint cierra el partido y dispara el calculo automatico de puntos de
	 * todos los pronosticos asociados. El cliente solo envia los goles reales; el
	 * backend calcula tendencia, puntos y nuevo estado de la fecha.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador del partido.
	 * @param request body JSON con homeGoals y awayGoals reales.
	 * @return partido finalizado con su resultado como DTO.
	 */
	@PatchMapping("/{id}/result") // Atiende PATCH /api/matches/{id}/result.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede cargar resultados.
	public MatchResponse loadResult(
			@PathVariable Long id,
			@Valid // Valida que los goles esten presentes y no sean negativos.
			@RequestBody MatchResultRequest request
	) {
		return matchService.loadResult(id, request);
	}

	/**
	 * Elimina un partido si todavia esta POR_JUGARSE.
	 *
	 * Rol permitido: ADMIN.
	 *
	 * @param id identificador del partido a eliminar.
	 */
	@DeleteMapping("/{id}") // Atiende DELETE /api/matches/{id}.
	@ResponseStatus(HttpStatus.NO_CONTENT) // Devuelve 204 al eliminar correctamente.
	@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN puede eliminar partidos.
	public void deleteMatch(
			@PathVariable Long id
	) {
		matchService.deleteMatch(id);
	}
}
