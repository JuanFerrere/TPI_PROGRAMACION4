package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.KnockoutRound;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchPhase;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutAdvanceRequest;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutBracketResponse;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutGenerateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutMatchResponse;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutRoundResponse;
import ar.edu.utn.frvm.prode.tournament.dto.StandingRowResponse;
import ar.edu.utn.frvm.prode.tournament.dto.StandingsGroupResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStandingsResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para generar y consultar la primera ronda eliminatoria de un torneo.
 */
@Service
public class TournamentKnockoutService {

	private static final int MATCH_SPACING_HOURS = 2;

	private final TournamentRepository tournamentRepository;
	private final TournamentTeamRepository tournamentTeamRepository;
	private final MatchRepository matchRepository;
	private final MatchDayRepository matchDayRepository;
	private final TournamentStandingService tournamentStandingService;

	public TournamentKnockoutService(
			TournamentRepository tournamentRepository,
			TournamentTeamRepository tournamentTeamRepository,
			MatchRepository matchRepository,
			MatchDayRepository matchDayRepository,
			TournamentStandingService tournamentStandingService
	) {
		this.tournamentRepository = tournamentRepository;
		this.tournamentTeamRepository = tournamentTeamRepository;
		this.matchRepository = matchRepository;
		this.matchDayRepository = matchDayRepository;
		this.tournamentStandingService = tournamentStandingService;
	}

	@Transactional
	public KnockoutBracketResponse generate(Long tournamentId, KnockoutGenerateRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		validateCanGenerate(tournament, request);

		TournamentStandingsResponse standings = tournamentStandingService.getStandings(tournamentId);
		List<MatchPair> pairs = buildPairs(tournamentId, standings, request);
		KnockoutRound round = roundForQualifiers(request.qualifiersCount());
		String roundLabel = labelForRound(round);

		if (matchDayRepository.existsByTournamentIdAndNameIgnoreCase(tournamentId, roundLabel)) {
			throw new BusinessRuleException("Ya existe una fecha para la ronda eliminatoria");
		}

		MatchDay matchDay = matchDayRepository.save(new MatchDay(roundLabel, tournament, null));

		for (int index = 0; index < pairs.size(); index++) {
			MatchPair pair = pairs.get(index);
			Match match = new Match(
					tournament,
					matchDay,
					pair.home().tournamentTeam().getTeam(),
					pair.away().tournamentTeam().getTeam(),
					request.firstRoundStartTime().plus((long) index * MATCH_SPACING_HOURS, ChronoUnit.HOURS)
			);
			match.setStatus(MatchStatus.POR_JUGARSE);
			match.setPhase(MatchPhase.KNOCKOUT);
			match.setKnockoutRound(round);
			match.setBracketPosition(index + 1);
			match.setHomeGoals(null);
			match.setAwayGoals(null);
			match.setResultTrend(null);
			matchRepository.save(match);
		}

		return getBracket(tournamentId);
	}

	@Transactional(readOnly = true)
	public KnockoutBracketResponse getBracket(Long tournamentId) {
		Tournament tournament = getTournamentById(tournamentId);
		List<Match> matches = matchRepository
				.findByTournamentIdAndPhaseOrderByKnockoutRoundAscBracketPositionAscStartTimeAsc(
						tournamentId,
						MatchPhase.KNOCKOUT
				);

		List<Match> orderedMatches = matches.stream()
				.sorted(Comparator
						.comparingInt((Match match) -> roundOrder(match.getKnockoutRound()))
						.thenComparing(match -> match.getBracketPosition(), Comparator.nullsLast(Comparator.naturalOrder()))
						.thenComparing(Match::getStartTime))
				.toList();

		Map<KnockoutRound, List<Match>> matchesByRound = new LinkedHashMap<>();
		for (Match match : orderedMatches) {
			matchesByRound.computeIfAbsent(match.getKnockoutRound(), key -> new ArrayList<>()).add(match);
		}

		List<KnockoutRoundResponse> rounds = matchesByRound.entrySet()
				.stream()
				.map(entry -> new KnockoutRoundResponse(
						entry.getKey(),
						labelForRound(entry.getKey()),
						entry.getValue().stream().map(this::toMatchResponse).toList()
				))
				.toList();

		return new KnockoutBracketResponse(tournament.getId(), tournament.getName(), rounds);
	}

	@Transactional
	public KnockoutBracketResponse advance(Long tournamentId, KnockoutAdvanceRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		validateCanAdvance(tournament, request);

		List<Match> knockoutMatches = matchRepository
				.findByTournamentIdAndPhaseOrderByKnockoutRoundAscBracketPositionAscStartTimeAsc(
						tournamentId,
						MatchPhase.KNOCKOUT
				);
		KnockoutRound currentRound = currentRoundForAdvance(knockoutMatches);
		KnockoutRound nextRound = nextRound(currentRound);

		if (nextRound == null) {
			throw new BusinessRuleException("El torneo ya tiene una final generada o finalizada");
		}

		if (hasRound(knockoutMatches, nextRound)) {
			throw new BusinessRuleException("La ronda siguiente ya fue generada");
		}

		List<Match> currentRoundMatches = knockoutMatches.stream()
				.filter(match -> match.getKnockoutRound() == currentRound)
				.sorted(Comparator
						.comparing((Match match) -> match.getBracketPosition(), Comparator.nullsLast(Comparator.naturalOrder()))
						.thenComparing(Match::getStartTime))
				.toList();

		validateCurrentRoundCompleted(currentRoundMatches);
		createNextRoundMatches(tournament, currentRoundMatches, nextRound, request.nextRoundStartTime());

		return getBracket(tournamentId);
	}

	private void validateCanAdvance(Tournament tournament, KnockoutAdvanceRequest request) {
		if (tournament.getStatus() != TournamentStatus.ACTIVE) {
			throw new BusinessRuleException("Solo se pueden avanzar eliminatorias en torneos ACTIVE");
		}

		if (request.nextRoundStartTime() == null) {
			throw new BusinessRuleException("El horario de la siguiente ronda es obligatorio");
		}
	}

	private KnockoutRound currentRoundForAdvance(List<Match> knockoutMatches) {
		if (knockoutMatches.isEmpty()) {
			throw new BusinessRuleException("El torneo no tiene una llave eliminatoria generada");
		}

		return knockoutMatches.stream()
				.map(Match::getKnockoutRound)
				.distinct()
				.max(Comparator.comparingInt(this::roundOrder))
				.orElseThrow(() -> new BusinessRuleException("El torneo no tiene una llave eliminatoria generada"));
	}

	private boolean hasRound(List<Match> matches, KnockoutRound round) {
		return matches.stream().anyMatch(match -> match.getKnockoutRound() == round);
	}

	private void validateCurrentRoundCompleted(List<Match> currentRoundMatches) {
		for (Match match : currentRoundMatches) {
			if (match.getStatus() != MatchStatus.FINALIZADO) {
				throw new BusinessRuleException("Todos los partidos de la ronda deben estar finalizados");
			}

			if (match.getWinnerTeam() == null) {
				throw new BusinessRuleException("Todos los partidos de la ronda deben tener ganador");
			}
		}
	}

	private void createNextRoundMatches(
			Tournament tournament,
			List<Match> currentRoundMatches,
			KnockoutRound nextRound,
			Instant nextRoundStartTime
	) {
		if (currentRoundMatches.size() % 2 != 0) {
			throw new BusinessRuleException("La cantidad de ganadores debe ser par para avanzar de ronda");
		}

		MatchDay matchDay = matchDayRepository.save(new MatchDay(
				uniqueMatchDayName(tournament.getId(), labelForRound(nextRound)),
				tournament,
				null
		));

		for (int index = 0; index < currentRoundMatches.size(); index += 2) {
			Match firstWinnerMatch = currentRoundMatches.get(index);
			Match secondWinnerMatch = currentRoundMatches.get(index + 1);
			int bracketPosition = (index / 2) + 1;

			Match match = new Match(
					tournament,
					matchDay,
					firstWinnerMatch.getWinnerTeam(),
					secondWinnerMatch.getWinnerTeam(),
					nextRoundStartTime.plus((long) (bracketPosition - 1) * MATCH_SPACING_HOURS, ChronoUnit.HOURS)
			);
			match.setStatus(MatchStatus.POR_JUGARSE);
			match.setPhase(MatchPhase.KNOCKOUT);
			match.setKnockoutRound(nextRound);
			match.setBracketPosition(bracketPosition);
			match.setHomeGoals(null);
			match.setAwayGoals(null);
			match.setResultTrend(null);
			match.setWinnerTeam(null);
			matchRepository.save(match);
		}
	}

	private String uniqueMatchDayName(Long tournamentId, String baseName) {
		if (!matchDayRepository.existsByTournamentIdAndNameIgnoreCase(tournamentId, baseName)) {
			return baseName;
		}

		int suffix = 2;
		String candidate = baseName + " " + suffix;
		while (matchDayRepository.existsByTournamentIdAndNameIgnoreCase(tournamentId, candidate)) {
			suffix++;
			candidate = baseName + " " + suffix;
		}

		return candidate;
	}

	private void validateCanGenerate(Tournament tournament, KnockoutGenerateRequest request) {
		if (tournament.getStatus() != TournamentStatus.ACTIVE) {
			throw new BusinessRuleException("Solo se pueden generar eliminatorias en torneos ACTIVE");
		}

		if (request.qualifiersCount() == null || !List.of(4, 8, 16).contains(request.qualifiersCount())) {
			throw new BusinessRuleException("La cantidad de clasificados debe ser 4, 8 o 16");
		}

		if (request.firstRoundStartTime() == null) {
			throw new BusinessRuleException("El horario de la primera ronda es obligatorio");
		}

		if (matchRepository.existsByTournamentIdAndPhase(tournament.getId(), MatchPhase.KNOCKOUT)) {
			throw new BusinessRuleException("El torneo ya tiene una llave eliminatoria generada");
		}
	}

	private List<MatchPair> buildPairs(
			Long tournamentId,
			TournamentStandingsResponse standings,
			KnockoutGenerateRequest request
	) {
		if (standings.format() == TournamentFormat.GROUPS) {
			return buildGroupPairs(tournamentId, standings, request);
		}

		return buildLeaguePairs(tournamentId, standings, request.qualifiersCount());
	}

	private List<MatchPair> buildLeaguePairs(
			Long tournamentId,
			TournamentStandingsResponse standings,
			Integer qualifiersCount
	) {
		if (standings.groups().isEmpty()) {
			throw new BusinessRuleException("El torneo no tiene tabla deportiva para generar eliminatorias");
		}

		List<StandingRowResponse> rows = standings.groups().getFirst().rows();

		if (rows.size() < qualifiersCount) {
			throw new BusinessRuleException("No hay suficientes equipos clasificados para generar eliminatorias");
		}

		List<Qualifier> qualifiers = rows.stream()
				.limit(qualifiersCount)
				.map(row -> toQualifier(tournamentId, row))
				.toList();

		return seedPairs(qualifiers);
	}

	private List<MatchPair> buildGroupPairs(
			Long tournamentId,
			TournamentStandingsResponse standings,
			KnockoutGenerateRequest request
	) {
		Integer qualifiedPerGroup = request.qualifiedPerGroup();

		if (qualifiedPerGroup == null || qualifiedPerGroup <= 0) {
			throw new BusinessRuleException("La cantidad de clasificados por grupo es obligatoria");
		}

		if (standings.groups().size() == 2 && qualifiedPerGroup == 2 && request.qualifiersCount() == 4) {
			StandingsGroupResponse firstGroup = standings.groups().get(0);
			StandingsGroupResponse secondGroup = standings.groups().get(1);
			validateGroupHasEnoughRows(firstGroup, qualifiedPerGroup);
			validateGroupHasEnoughRows(secondGroup, qualifiedPerGroup);

			Qualifier firstGroupWinner = toQualifier(tournamentId, firstGroup.rows().get(0));
			Qualifier firstGroupRunnerUp = toQualifier(tournamentId, firstGroup.rows().get(1));
			Qualifier secondGroupWinner = toQualifier(tournamentId, secondGroup.rows().get(0));
			Qualifier secondGroupRunnerUp = toQualifier(tournamentId, secondGroup.rows().get(1));

			return List.of(
					new MatchPair(firstGroupWinner, secondGroupRunnerUp),
					new MatchPair(secondGroupWinner, firstGroupRunnerUp)
			);
		}

		List<Qualifier> qualifiers = new ArrayList<>();
		for (StandingsGroupResponse group : standings.groups()) {
			validateGroupHasEnoughRows(group, qualifiedPerGroup);
			group.rows().stream()
					.limit(qualifiedPerGroup)
					.map(row -> toQualifier(tournamentId, row))
					.forEach(qualifiers::add);
		}

		if (qualifiers.size() != request.qualifiersCount()) {
			throw new BusinessRuleException("La cantidad de clasificados por grupo no coincide con el total solicitado");
		}

		List<Qualifier> seeded = qualifiers.stream()
				.sorted(Comparator
						.comparingInt((Qualifier qualifier) -> qualifier.row().points()).reversed()
						.thenComparing(Comparator.comparingInt((Qualifier qualifier) -> qualifier.row().goalDifference()).reversed())
						.thenComparing(Comparator.comparingInt((Qualifier qualifier) -> qualifier.row().goalsFor()).reversed())
						.thenComparing(qualifier -> qualifier.row().groupName(), Comparator.nullsLast(Comparator.naturalOrder()))
						.thenComparing(qualifier -> qualifier.row().position())
						.thenComparing(qualifier -> qualifier.row().teamName().toLowerCase()))
				.toList();

		return seedPairs(seeded);
	}

	private List<MatchPair> seedPairs(List<Qualifier> qualifiers) {
		List<MatchPair> pairs = new ArrayList<>();

		for (int index = 0; index < qualifiers.size() / 2; index++) {
			pairs.add(new MatchPair(qualifiers.get(index), qualifiers.get(qualifiers.size() - 1 - index)));
		}

		return pairs;
	}

	private void validateGroupHasEnoughRows(StandingsGroupResponse group, int qualifiedPerGroup) {
		if (group.rows().size() < qualifiedPerGroup) {
			throw new BusinessRuleException("No hay suficientes equipos clasificados en un grupo");
		}
	}

	private Qualifier toQualifier(Long tournamentId, StandingRowResponse row) {
		TournamentTeam tournamentTeam = tournamentTeamRepository
				.findByTournamentIdAndTeamId(tournamentId, row.teamId())
				.orElseThrow(() -> new ResourceNotFoundException("Equipo clasificado no pertenece al torneo"));
		return new Qualifier(row, tournamentTeam);
	}

	private KnockoutRound roundForQualifiers(Integer qualifiersCount) {
		return switch (qualifiersCount) {
			case 16 -> KnockoutRound.ROUND_OF_16;
			case 8 -> KnockoutRound.QUARTER_FINAL;
			case 4 -> KnockoutRound.SEMI_FINAL;
			default -> throw new BusinessRuleException("La cantidad de clasificados debe ser 4, 8 o 16");
		};
	}

	private KnockoutRound nextRound(KnockoutRound round) {
		if (round == null) {
			return null;
		}

		return switch (round) {
			case ROUND_OF_16 -> KnockoutRound.QUARTER_FINAL;
			case QUARTER_FINAL -> KnockoutRound.SEMI_FINAL;
			case SEMI_FINAL -> KnockoutRound.FINAL;
			case FINAL -> null;
		};
	}

	private String labelForRound(KnockoutRound round) {
		if (round == null) {
			return "Eliminatoria";
		}

		return switch (round) {
			case ROUND_OF_16 -> "Octavos de final";
			case QUARTER_FINAL -> "Cuartos de final";
			case SEMI_FINAL -> "Semifinales";
			case FINAL -> "Final";
		};
	}

	private int roundOrder(KnockoutRound round) {
		if (round == null) {
			return Integer.MAX_VALUE;
		}

		return switch (round) {
			case ROUND_OF_16 -> 1;
			case QUARTER_FINAL -> 2;
			case SEMI_FINAL -> 3;
			case FINAL -> 4;
		};
	}

	private Tournament getTournamentById(Long tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
	}

	private KnockoutMatchResponse toMatchResponse(Match match) {
		return new KnockoutMatchResponse(
				match.getId(),
				match.getBracketPosition(),
				match.getHomeTeam().getId(),
				match.getHomeTeam().getName(),
				match.getAwayTeam().getId(),
				match.getAwayTeam().getName(),
				match.getStartTime(),
				match.getStatus(),
				match.getHomeGoals(),
				match.getAwayGoals(),
				match.getResultTrend(),
				match.getWinnerTeam() == null ? null : match.getWinnerTeam().getId(),
				match.getWinnerTeam() == null ? null : match.getWinnerTeam().getName()
		);
	}

	private record Qualifier(StandingRowResponse row, TournamentTeam tournamentTeam) {
	}

	private record MatchPair(Qualifier home, Qualifier away) {
	}
}
