package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchBulkItemRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service para administrar partidos dentro de un torneo.
 */
@Service
public class TournamentMatchService {

	private final TournamentRepository tournamentRepository;
	private final MatchDayRepository matchDayRepository;
	private final MatchRepository matchRepository;
	private final TournamentTeamRepository tournamentTeamRepository;
	private final PredictionRepository predictionRepository;

	public TournamentMatchService(
			TournamentRepository tournamentRepository,
			MatchDayRepository matchDayRepository,
			MatchRepository matchRepository,
			TournamentTeamRepository tournamentTeamRepository,
			PredictionRepository predictionRepository
	) {
		this.tournamentRepository = tournamentRepository;
		this.matchDayRepository = matchDayRepository;
		this.matchRepository = matchRepository;
		this.tournamentTeamRepository = tournamentTeamRepository;
		this.predictionRepository = predictionRepository;
	}

	@Transactional(readOnly = true)
	public List<TournamentMatchResponse> findAll(Long tournamentId) {
		getTournamentById(tournamentId);

		return matchRepository.findByTournamentIdOrderByStartTimeAsc(tournamentId)
				.stream()
				.map(match -> toResponse(tournamentId, match))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TournamentMatchResponse> findByMatchDay(Long tournamentId, Long matchDayId) {
		getTournamentById(tournamentId);
		getMatchDayById(tournamentId, matchDayId);

		return matchRepository.findByTournamentIdAndMatchDayIdOrderByStartTimeAsc(tournamentId, matchDayId)
				.stream()
				.map(match -> toResponse(tournamentId, match))
				.toList();
	}

	@Transactional
	public TournamentMatchResponse create(Long tournamentId, TournamentMatchCreateRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		MatchDay matchDay = getMatchDayById(tournamentId, request.matchDayId());
		TournamentTeam homeTournamentTeam = getTournamentTeamById(tournamentId, request.homeTournamentTeamId(), "Equipo local no pertenece al torneo");
		TournamentTeam awayTournamentTeam = getTournamentTeamById(tournamentId, request.awayTournamentTeamId(), "Equipo visitante no pertenece al torneo");

		Match match = createInternal(
				tournament,
				matchDay,
				homeTournamentTeam,
				awayTournamentTeam,
				request.startTime(),
				null
		);

		return toResponse(tournamentId, match, homeTournamentTeam, awayTournamentTeam);
	}

	@Transactional
	public List<TournamentMatchResponse> createBulk(Long tournamentId, TournamentMatchBulkCreateRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		MatchDay matchDay = getMatchDayById(tournamentId, request.matchDayId());
		Set<String> pairKeys = new HashSet<>();

		for (int index = 0; index < request.matches().size(); index++) {
			TournamentMatchBulkItemRequest item = request.matches().get(index);
			int itemNumber = index + 1;

			if (Objects.equals(item.homeTournamentTeamId(), item.awayTournamentTeamId())) {
				throw new BusinessRuleException("Item " + itemNumber + ": el equipo local y visitante no pueden ser el mismo");
			}

			String pairKey = buildTournamentTeamPairKey(item.homeTournamentTeamId(), item.awayTournamentTeamId());

			if (!pairKeys.add(pairKey)) {
				throw new DuplicateResourceException("Item " + itemNumber + ": hay un partido repetido o invertido en la carga");
			}
		}

		return request.matches()
				.stream()
				.map(item -> {
					TournamentTeam homeTournamentTeam = getTournamentTeamById(tournamentId, item.homeTournamentTeamId(), "Equipo local no pertenece al torneo");
					TournamentTeam awayTournamentTeam = getTournamentTeamById(tournamentId, item.awayTournamentTeamId(), "Equipo visitante no pertenece al torneo");
					Match match = createInternal(
							tournament,
							matchDay,
							homeTournamentTeam,
							awayTournamentTeam,
							item.startTime(),
							null
					);
					return toResponse(tournamentId, match, homeTournamentTeam, awayTournamentTeam);
				})
				.toList();
	}

	@Transactional
	public void remove(Long tournamentId, Long matchId) {
		getTournamentById(tournamentId);
		Match match = getMatchById(tournamentId, matchId);

		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede eliminar un partido que no esta POR_JUGARSE");
		}

		if (match.getHomeGoals() != null || match.getAwayGoals() != null || match.getResultTrend() != null) {
			throw new BusinessRuleException("No se puede eliminar un partido con resultado asociado");
		}

		if (predictionRepository.existsByMatchId(matchId)) {
			throw new BusinessRuleException("No se puede eliminar el partido porque tiene pronosticos asociados");
		}

		matchRepository.delete(match);
	}

	private Match createInternal(
			Tournament tournament,
			MatchDay matchDay,
			TournamentTeam homeTournamentTeam,
			TournamentTeam awayTournamentTeam,
			Instant startTime,
			Integer itemNumber
	) {
		validateStartTime(startTime, itemNumber);
		validateDifferentTeams(homeTournamentTeam, awayTournamentTeam, itemNumber);

		if (!Objects.equals(matchDay.getTournament().getId(), tournament.getId())) {
			throw new BusinessRuleException(buildItemMessage("La fecha no pertenece al torneo", itemNumber));
		}

		if (matchRepository.existsDuplicatedTeamsInMatchDay(
				matchDay.getId(),
				homeTournamentTeam.getTeam().getId(),
				awayTournamentTeam.getTeam().getId()
		)) {
			throw new DuplicateResourceException(buildItemMessage("Ya existe un partido entre esos equipos en esta fecha", itemNumber));
		}

		Match match = new Match(
				tournament,
				matchDay,
				homeTournamentTeam.getTeam(),
				awayTournamentTeam.getTeam(),
				startTime
		);
		match.setStatus(MatchStatus.POR_JUGARSE);
		return matchRepository.save(match);
	}

	private Tournament getTournamentById(Long tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
	}

	private MatchDay getMatchDayById(Long tournamentId, Long matchDayId) {
		return matchDayRepository.findByIdAndTournamentId(matchDayId, tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Fecha del torneo no encontrada"));
	}

	private Match getMatchById(Long tournamentId, Long matchId) {
		return matchRepository.findByIdAndTournamentId(matchId, tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido del torneo no encontrado"));
	}

	private TournamentTeam getTournamentTeamById(Long tournamentId, Long tournamentTeamId, String message) {
		return tournamentTeamRepository.findByIdAndTournamentIdWithTeam(tournamentTeamId, tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException(message));
	}

	private TournamentTeam getTournamentTeamByTeamId(Long tournamentId, Long teamId) {
		return tournamentTeamRepository.findByTournamentIdAndTeamId(tournamentId, teamId)
				.orElseThrow(() -> new ResourceNotFoundException("Equipo del torneo no encontrado"));
	}

	private void validateDifferentTeams(
			TournamentTeam homeTournamentTeam,
			TournamentTeam awayTournamentTeam,
			Integer itemNumber
	) {
		if (Objects.equals(homeTournamentTeam.getTeam().getId(), awayTournamentTeam.getTeam().getId())) {
			throw new BusinessRuleException(buildItemMessage("El equipo local y el equipo visitante no pueden ser el mismo", itemNumber));
		}
	}

	private void validateStartTime(Instant startTime, Integer itemNumber) {
		if (startTime == null) {
			throw new BusinessRuleException(buildItemMessage("El horario de inicio es obligatorio", itemNumber));
		}
	}

	private String buildTournamentTeamPairKey(Long homeTournamentTeamId, Long awayTournamentTeamId) {
		Long min = Math.min(homeTournamentTeamId, awayTournamentTeamId);
		Long max = Math.max(homeTournamentTeamId, awayTournamentTeamId);
		return min + ":" + max;
	}

	private String buildItemMessage(String message, Integer itemNumber) {
		if (itemNumber == null) {
			return message;
		}

		return "Item " + itemNumber + ": " + message;
	}

	private TournamentMatchResponse toResponse(Long tournamentId, Match match) {
		TournamentTeam homeTournamentTeam = getTournamentTeamByTeamId(tournamentId, match.getHomeTeam().getId());
		TournamentTeam awayTournamentTeam = getTournamentTeamByTeamId(tournamentId, match.getAwayTeam().getId());
		return toResponse(tournamentId, match, homeTournamentTeam, awayTournamentTeam);
	}

	private TournamentMatchResponse toResponse(
			Long tournamentId,
			Match match,
			TournamentTeam homeTournamentTeam,
			TournamentTeam awayTournamentTeam
	) {
		return new TournamentMatchResponse(
				match.getId(),
				tournamentId,
				match.getMatchDay().getId(),
				match.getMatchDay().getName(),
				homeTournamentTeam.getTeam().getId(),
				homeTournamentTeam.getId(),
				homeTournamentTeam.getTeam().getName(),
				homeTournamentTeam.getGroupName(),
				awayTournamentTeam.getTeam().getId(),
				awayTournamentTeam.getId(),
				awayTournamentTeam.getTeam().getName(),
				awayTournamentTeam.getGroupName(),
				match.getStartTime(),
				match.getStatus()
		);
	}
}
