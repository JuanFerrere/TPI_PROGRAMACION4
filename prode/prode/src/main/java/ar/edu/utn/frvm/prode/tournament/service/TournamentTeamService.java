package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.team.repository.TeamRepository;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamGroupUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Service para administrar equipos dentro de un torneo.
 *
 * La entidad Team sigue siendo global. Este service crea o reutiliza Team y
 * administra solo la participacion en TournamentTeam.
 */
@Service
public class TournamentTeamService {

	private static final int MAX_TEAM_NAME_LENGTH = 100;
	private static final int MAX_GROUP_NAME_LENGTH = 20;

	private final TournamentRepository tournamentRepository;
	private final TournamentTeamRepository tournamentTeamRepository;
	private final TeamRepository teamRepository;

	public TournamentTeamService(
			TournamentRepository tournamentRepository,
			TournamentTeamRepository tournamentTeamRepository,
			TeamRepository teamRepository
	) {
		this.tournamentRepository = tournamentRepository;
		this.tournamentTeamRepository = tournamentTeamRepository;
		this.teamRepository = teamRepository;
	}

	@Transactional(readOnly = true)
	public List<TournamentTeamResponse> findByTournament(Long tournamentId) {
		getTournamentById(tournamentId);

		return tournamentTeamRepository.findByTournamentIdOrdered(tournamentId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public TournamentTeamResponse addTeam(Long tournamentId, TournamentTeamCreateRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		return toResponse(addTeamInternal(tournament, request.name(), request.groupName(), null));
	}

	@Transactional
	public List<TournamentTeamResponse> addTeamsBulk(Long tournamentId, TournamentTeamBulkCreateRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		List<ParsedTeamLine> lines = parseBulkContent(tournament, request.content());
		List<TournamentTeamResponse> responses = new ArrayList<>();

		for (ParsedTeamLine line : lines) {
			TournamentTeam tournamentTeam = addTeamInternal(tournament, line.teamName(), line.groupName(), line.lineNumber());
			responses.add(toResponse(tournamentTeam));
		}

		return responses;
	}

	@Transactional
	public TournamentTeamResponse updateGroup(
			Long tournamentId,
			Long tournamentTeamId,
			TournamentTeamGroupUpdateRequest request
	) {
		Tournament tournament = getTournamentById(tournamentId);

		if (effectiveFormat(tournament) != TournamentFormat.GROUPS) {
			throw new BusinessRuleException("Los torneos de tabla general no usan grupos");
		}

		TournamentTeam tournamentTeam = getTournamentTeamById(tournamentId, tournamentTeamId);
		tournamentTeam.setGroupName(normalizeGroupNameForGroups(request.groupName(), null));
		return toResponse(tournamentTeam);
	}

	@Transactional
	public void removeTeam(Long tournamentId, Long tournamentTeamId) {
		getTournamentById(tournamentId);
		TournamentTeam tournamentTeam = getTournamentTeamById(tournamentId, tournamentTeamId);
		tournamentTeamRepository.delete(tournamentTeam);
	}

	private TournamentTeam addTeamInternal(
			Tournament tournament,
			String rawTeamName,
			String rawGroupName,
			Integer lineNumber
	) {
		String teamName = normalizeTeamName(rawTeamName, lineNumber);
		String groupName = normalizeGroupName(tournament, rawGroupName, lineNumber);
		Team team = getOrCreateTeam(teamName);

		if (tournamentTeamRepository.existsByTournamentIdAndTeamId(tournament.getId(), team.getId())) {
			throw new DuplicateResourceException(buildLineMessage("El equipo ya pertenece a este torneo", lineNumber));
		}

		TournamentTeam tournamentTeam = new TournamentTeam(tournament, team, groupName);
		return tournamentTeamRepository.save(tournamentTeam);
	}

	private List<ParsedTeamLine> parseBulkContent(Tournament tournament, String content) {
		if (content == null || content.isBlank()) {
			throw new BusinessRuleException("La carga masiva no puede estar vacia");
		}

		String[] rawLines = content.split("\\R");
		List<ParsedTeamLine> parsedLines = new ArrayList<>();

		for (int index = 0; index < rawLines.length; index++) {
			String line = rawLines[index].trim();

			if (line.isBlank()) {
				continue;
			}

			int lineNumber = index + 1;

			if (effectiveFormat(tournament) == TournamentFormat.GROUPS) {
				String[] parts = line.split("\\|", -1);

				if (parts.length != 2) {
					throw new BusinessRuleException("Linea " + lineNumber + ": usar formato Equipo|Grupo");
				}

				parsedLines.add(new ParsedTeamLine(parts[0], parts[1], lineNumber));
			} else {
				if (line.contains("|")) {
					throw new BusinessRuleException("Linea " + lineNumber + ": en tabla general usar solo el nombre del equipo");
				}

				parsedLines.add(new ParsedTeamLine(line, null, lineNumber));
			}
		}

		if (parsedLines.isEmpty()) {
			throw new BusinessRuleException("La carga masiva no tiene equipos validos");
		}

		return parsedLines;
	}

	private Team getOrCreateTeam(String name) {
		return teamRepository.findByNameIgnoreCase(name)
				.orElseGet(() -> teamRepository.save(new Team(name)));
	}

	private Tournament getTournamentById(Long tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
	}

	private TournamentTeam getTournamentTeamById(Long tournamentId, Long tournamentTeamId) {
		return tournamentTeamRepository.findByIdAndTournamentId(tournamentTeamId, tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Equipo del torneo no encontrado"));
	}

	private TournamentFormat effectiveFormat(Tournament tournament) {
		return tournament.getFormat() == null ? TournamentFormat.LEAGUE : tournament.getFormat();
	}

	private String normalizeTeamName(String name, Integer lineNumber) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException(buildLineMessage("El nombre del equipo es obligatorio", lineNumber));
		}

		String normalizedName = name.trim();

		if (normalizedName.length() > MAX_TEAM_NAME_LENGTH) {
			throw new BusinessRuleException(buildLineMessage("El nombre del equipo no puede superar 100 caracteres", lineNumber));
		}

		return normalizedName;
	}

	private String normalizeGroupName(Tournament tournament, String groupName, Integer lineNumber) {
		if (effectiveFormat(tournament) == TournamentFormat.GROUPS) {
			return normalizeGroupNameForGroups(groupName, lineNumber);
		}

		if (groupName != null && !groupName.isBlank()) {
			throw new BusinessRuleException(buildLineMessage("Los torneos de tabla general no usan grupos", lineNumber));
		}

		return null;
	}

	private String normalizeGroupNameForGroups(String groupName, Integer lineNumber) {
		if (groupName == null || groupName.isBlank()) {
			throw new BusinessRuleException(buildLineMessage("El grupo es obligatorio para torneos por grupos", lineNumber));
		}

		String normalizedGroupName = groupName.trim().toUpperCase(Locale.ROOT);

		if (normalizedGroupName.length() > MAX_GROUP_NAME_LENGTH) {
			throw new BusinessRuleException(buildLineMessage("El grupo no puede superar 20 caracteres", lineNumber));
		}

		return normalizedGroupName;
	}

	private String buildLineMessage(String message, Integer lineNumber) {
		if (lineNumber == null) {
			return message;
		}

		return "Linea " + lineNumber + ": " + message;
	}

	private TournamentTeamResponse toResponse(TournamentTeam tournamentTeam) {
		return new TournamentTeamResponse(
				tournamentTeam.getId(),
				tournamentTeam.getTournament().getId(),
				tournamentTeam.getTeam().getId(),
				tournamentTeam.getTeam().getName(),
				tournamentTeam.getGroupName(),
				tournamentTeam.getCreatedAt(),
				tournamentTeam.getUpdatedAt()
		);
	}

	private record ParsedTeamLine(
			String teamName,
			String groupName,
			Integer lineNumber
	) {
	}
}
