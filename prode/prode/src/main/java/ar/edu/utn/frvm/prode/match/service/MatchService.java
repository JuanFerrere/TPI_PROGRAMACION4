package ar.edu.utn.frvm.prode.match.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.dto.MatchCreateRequest;
import ar.edu.utn.frvm.prode.match.dto.MatchResponse;
import ar.edu.utn.frvm.prode.match.dto.MatchResultRequest;
import ar.edu.utn.frvm.prode.match.dto.MatchUpdateRequest;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.matchday.service.MatchDayService;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.prediction.service.PredictionScoringService;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class MatchService {
	private final MatchRepository matchRepository;
	private final PredictionRepository predictionRepository;
	private final MatchDayRepository matchDayRepository;
	private final TeamRepository teamRepository;
	private final MatchDayService matchDayService;
	private final PredictionScoringService predictionScoringService;

	public MatchService(
			MatchRepository matchRepository,
			PredictionRepository predictionRepository,
			MatchDayRepository matchDayRepository,
			TeamRepository teamRepository,
			MatchDayService matchDayService,
			PredictionScoringService predictionScoringService
	) {
		this.matchRepository = matchRepository;
		this.predictionRepository = predictionRepository;
		this.matchDayRepository = matchDayRepository;
		this.teamRepository = teamRepository;
		this.matchDayService = matchDayService;
		this.predictionScoringService = predictionScoringService;
	}

	@Transactional
	public MatchResponse createMatch(MatchCreateRequest request) {
		validateDifferentTeams(request.homeTeamId(), request.awayTeamId());

		MatchDay matchDay = getMatchDayEntityById(request.matchDayId());
		validateMatchDayIsScheduled(matchDay);
		Team homeTeam = getTeamEntityById(request.homeTeamId(), "Equipo local no encontrado");
		Team awayTeam = getTeamEntityById(request.awayTeamId(), "Equipo visitante no encontrado");
		Instant startTime = validateStartTime(request.startTime());

		Match match = new Match(matchDay, homeTeam, awayTeam, startTime);
		match.setStatus(MatchStatus.POR_JUGARSE);

		Match savedMatch = matchRepository.save(match);
		matchDayService.refreshStatusByMatchDayId(matchDay.getId());
		return toResponse(savedMatch);
	}

	@Transactional
	public MatchResponse updateMatch(Long id, MatchUpdateRequest request) {
		Match match = getMatchEntityById(id);

		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede modificar un partido que ya comenzo");
		}

		validateMatchHasNoPredictions(
				match.getId(),
				"No se puede modificar el partido porque ya tiene pronosticos asociados"
		);

		validateDifferentTeams(request.homeTeamId(), request.awayTeamId());

		Team homeTeam = getTeamEntityById(request.homeTeamId(), "Equipo local no encontrado");
		Team awayTeam = getTeamEntityById(request.awayTeamId(), "Equipo visitante no encontrado");
		Instant startTime = validateStartTime(request.startTime());

		match.setHomeTeam(homeTeam);
		match.setAwayTeam(awayTeam);
		match.setStartTime(startTime);

		return toResponse(match);
	}

	@Transactional
	public MatchResponse startMatch(Long id) {
		Match match = getMatchEntityById(id);

		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede iniciar un partido que no esta POR_JUGARSE");
		}

		match.setStatus(MatchStatus.EN_JUEGO);
		Match savedMatch = matchRepository.save(match);

		matchDayService.refreshStatusByMatchDayId(savedMatch.getMatchDay().getId());

		return toResponse(savedMatch);
	}

	@Transactional
	public MatchResponse loadResult(Long id, MatchResultRequest request) {
		Match match = getMatchEntityById(id);

		if (match.getStatus() == MatchStatus.FINALIZADO) {
			throw new BusinessRuleException("No se puede cargar resultado porque el partido ya esta FINALIZADO");
		}
		if (match.getStatus() != MatchStatus.EN_JUEGO) {
			throw new BusinessRuleException("Solo se pueden cargar resultados de partidos que estan EN_JUEGO");
		}

		ResultTrend resultTrend = calculateResultTrend(request.homeGoals(), request.awayGoals());

		match.setHomeGoals(request.homeGoals());
		match.setAwayGoals(request.awayGoals());
		match.setResultTrend(resultTrend);
		match.setStatus(MatchStatus.FINALIZADO);
		Match savedMatch = matchRepository.save(match);

		predictionScoringService.scoreMatchPredictions(savedMatch);

		matchDayService.refreshStatusByMatchDayId(savedMatch.getMatchDay().getId());

		return toResponse(savedMatch);
	}

	@Transactional
	public void deleteMatch(Long id) {
		Match match = getMatchEntityById(id);

		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede eliminar un partido que no esta POR_JUGARSE");
		}

		validateMatchHasNoPredictions(
				match.getId(),
				"No se puede eliminar el partido porque tiene pronosticos asociados"
		);

		Long matchDayId = match.getMatchDay().getId();
		matchRepository.delete(match);
		matchDayService.refreshStatusByMatchDayId(matchDayId);
	}

	@Transactional(readOnly = true)
	public List<MatchResponse> getAllMatches() {
		return matchRepository.findAllByOrderByStartTimeAsc()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MatchResponse> getMatchesByMatchDay(Long matchDayId) {
		getMatchDayEntityById(matchDayId);

		return matchRepository.findByMatchDayIdOrderByStartTimeAsc(matchDayId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public MatchResponse getMatchById(Long id) {
		return toResponse(getMatchEntityById(id));
	}

	private void validateDifferentTeams(Long homeTeamId, Long awayTeamId) {
		if (Objects.equals(homeTeamId, awayTeamId)) {
			throw new BusinessRuleException("El equipo local y el equipo visitante no pueden ser el mismo");
		}
	}

	private void validateMatchDayIsScheduled(MatchDay matchDay) {
		if (matchDay.getStatus() != MatchDayStatus.PROGRAMADA) {
			throw new BusinessRuleException("No se pueden crear partidos en una fecha que no esta en estado PROGRAMADA");
		}
	}

	private Instant validateStartTime(Instant startTime) {
		if (startTime == null) {
			throw new BusinessRuleException("El horario de inicio es obligatorio");
		}
		return startTime;
	}

	private ResultTrend calculateResultTrend(int homeGoals, int awayGoals) {
		if (homeGoals > awayGoals) {
			return ResultTrend.LOCAL;
		}

		if (homeGoals < awayGoals) {
			return ResultTrend.VISITANTE;
		}

		return ResultTrend.EMPATE;
	}

	private void validateMatchHasNoPredictions(Long matchId, String message) {
		if (predictionRepository.existsByMatchId(matchId)) {
			throw new BusinessRuleException(message);
		}
	}

	private MatchDay getMatchDayEntityById(Long id) {
		return matchDayRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada"));
	}

	private Team getTeamEntityById(Long id, String message) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(message));
	}

	private Match getMatchEntityById(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
	}

	private MatchResponse toResponse(Match match) {
		return new MatchResponse(
				match.getId(),
				match.getMatchDay().getId(),
				match.getMatchDay().getName(),
				match.getHomeTeam().getId(),
				match.getHomeTeam().getName(),
				match.getAwayTeam().getId(),
				match.getAwayTeam().getName(),
				match.getStartTime(),
				match.getStatus(),
				match.getHomeGoals(),
				match.getAwayGoals(),
				match.getResultTrend(),
				match.getCreatedAt(),
				match.getUpdatedAt()
		);
	}
}
