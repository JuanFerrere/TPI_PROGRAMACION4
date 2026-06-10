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

@Service
public class TeamService {
	private final TeamRepository teamRepository;
	private final MatchRepository matchRepository;

	public TeamService(TeamRepository teamRepository, MatchRepository matchRepository) {
		this.teamRepository = teamRepository;
		this.matchRepository = matchRepository;
	}

	@Transactional
	public TeamResponse createTeam(TeamCreateRequest request) {
		String name = normalizeName(request.name());

		if (teamRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe un equipo con ese nombre");
		}

		Team team = new Team(name);
		Team savedTeam = teamRepository.save(team);
		return toResponse(savedTeam);
	}

	@Transactional(readOnly = true)
	public List<TeamResponse> getAllTeams() {
		return teamRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TeamResponse> searchTeamsByName(String name) {
		if (name == null || name.isBlank()) {
			return getAllTeams();
		}

		return teamRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name.trim())
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public TeamResponse getTeamById(Long id) {
		return toResponse(getTeamEntityById(id));
	}

	@Transactional
	public void deleteTeam(Long id) {
		Team team = getTeamEntityById(id);

		if (matchRepository.existsByHomeTeamId(id)) {
			throw new BusinessRuleException("No se puede eliminar un equipo asociado a un partido como local");
		}

		if (matchRepository.existsByAwayTeamId(id)) {
			throw new BusinessRuleException("No se puede eliminar un equipo asociado a un partido como visitante");
		}

		teamRepository.delete(team);
	}

	private Team getTeamEntityById(Long id) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException("El nombre del equipo es obligatorio");
		}
		return name.trim();
	}

	private TeamResponse toResponse(Team team) {
		return new TeamResponse(
				team.getId(),
				team.getName(),
				team.getCreatedAt()
		);
	}
}
