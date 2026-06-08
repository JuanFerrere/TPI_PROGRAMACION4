package ar.edu.utn.frvm.prode.team.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.team.dto.TeamCreateRequest;
import ar.edu.utn.frvm.prode.team.dto.TeamResponse;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.team.repository.TeamRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de equipos.
 *
 * Contiene la logica de negocio de Team para que los controllers solo reciban HTTP y devuelvan DTOs.
 */
@Service // Spring registra esta clase como componente de logica de negocio.
public class TeamService {

	private final TeamRepository teamRepository;
	private final MatchRepository matchRepository;

	/**
	 * Constructor con repositorios necesarios.
	 *
	 * @param teamRepository repositorio para consultar y guardar equipos.
	 * @param matchRepository repositorio para validar si un equipo esta usado en partidos.
	 */
	public TeamService(TeamRepository teamRepository, MatchRepository matchRepository) {
		this.teamRepository = teamRepository;
		this.matchRepository = matchRepository;
	}

	/**
	 * Crea un equipo nuevo.
	 *
	 * @param request DTO de entrada con el nombre del equipo.
	 * @return DTO de salida con el equipo persistido.
	 */
	@Transactional // La creacion se ejecuta en una transaccion para guardar de forma consistente.
	public TeamResponse createTeam(TeamCreateRequest request) {
		String name = normalizeName(request.name());

		// Regla de negocio: no se permiten equipos duplicados aunque cambien mayusculas/minusculas.
		if (teamRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe un equipo con ese nombre");
		}

		Team team = new Team(name);
		Team savedTeam = teamRepository.save(team);
		return toResponse(savedTeam);
	}

	/**
	 * Lista todos los equipos ordenados alfabeticamente.
	 *
	 * No recibe parametros.
	 * @return lista de DTOs de equipos.
	 */
	@Transactional(readOnly = true) // Solo consulta datos; no modifica la base.
	public List<TeamResponse> getAllTeams() {
		return teamRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Busca equipos por nombre parcial.
	 *
	 * @param name texto opcional recibido por query param.
	 * @return lista de equipos filtrada; si name esta vacio devuelve todos.
	 */
	@Transactional(readOnly = true) // Busqueda sin cambios en base.
	public List<TeamResponse> searchTeamsByName(String name) {
		if (name == null || name.isBlank()) {
			return getAllTeams();
		}

		return teamRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name.trim())
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Obtiene un equipo por id.
	 *
	 * @param id identificador del equipo.
	 * @return DTO del equipo encontrado.
	 */
	@Transactional(readOnly = true) // Solo consulta por id.
	public TeamResponse getTeamById(Long id) {
		return toResponse(getTeamEntityById(id));
	}

	/**
	 * Elimina un equipo si no esta asociado a partidos.
	 *
	 * @param id identificador del equipo a eliminar.
	 */
	@Transactional // La validacion y el borrado se ejecutan juntos.
	public void deleteTeam(Long id) {
		Team team = getTeamEntityById(id);

		// Regla de negocio: no se elimina un equipo usado como local porque dejaria partidos inconsistentes.
		if (matchRepository.existsByHomeTeamId(id)) {
			throw new BusinessRuleException("No se puede eliminar un equipo asociado a un partido como local");
		}

		// Regla de negocio: no se elimina un equipo usado como visitante por la misma razon de integridad.
		if (matchRepository.existsByAwayTeamId(id)) {
			throw new BusinessRuleException("No se puede eliminar un equipo asociado a un partido como visitante");
		}

		teamRepository.delete(team);
	}

	/**
	 * Busca la entidad Team interna por id.
	 *
	 * @param id identificador del equipo.
	 * @return entidad Team encontrada.
	 */
	private Team getTeamEntityById(Long id) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
	}

	/**
	 * Normaliza nombres antes de guardarlos o compararlos.
	 *
	 * @param name nombre recibido desde el cliente.
	 * @return nombre sin espacios al inicio o final.
	 */
	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException("El nombre del equipo es obligatorio");
		}
		return name.trim();
	}

	/**
	 * Convierte entidad Team a DTO de salida.
	 *
	 * @param team entidad JPA interna.
	 * @return DTO seguro para responder al cliente.
	 */
	private TeamResponse toResponse(Team team) {
		return new TeamResponse(
				team.getId(),
				team.getName(),
				team.getCreatedAt()
		);
	}
}
