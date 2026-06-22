package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.match.entity.KnockoutRound;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchPhase;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutAdvanceRequest;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutBracketResponse;
import ar.edu.utn.frvm.prode.tournament.dto.KnockoutGenerateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.StandingRowResponse;
import ar.edu.utn.frvm.prode.tournament.dto.StandingsGroupResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStandingsResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TournamentKnockoutServiceTest {

	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final TournamentTeamRepository tournamentTeamRepository = mock(TournamentTeamRepository.class);
	private final MatchRepository matchRepository = mock(MatchRepository.class);
	private final MatchDayRepository matchDayRepository = mock(MatchDayRepository.class);
	private final TournamentStandingService tournamentStandingService = mock(TournamentStandingService.class);
	private final TournamentKnockoutService service = new TournamentKnockoutService(
			tournamentRepository,
			tournamentTeamRepository,
			matchRepository,
			matchDayRepository,
			tournamentStandingService
	);

	@Test
	void generaSemifinalesDesdeLeagueTopCuatro() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		List<TournamentTeam> tournamentTeams = tournamentTeams(tournament, "Alfa", "Beta", "Gamma", "Delta");
		List<Match> savedMatches = stubGeneration(tournament, leagueStandings(tournamentTeams), tournamentTeams);

		KnockoutBracketResponse response = service.generate(
				1L,
				new KnockoutGenerateRequest(4, null, Instant.parse("2026-07-01T20:00:00Z"))
		);

		assertEquals(1, response.rounds().size());
		assertEquals(KnockoutRound.SEMI_FINAL, response.rounds().getFirst().round());
		assertEquals("Semifinales", response.rounds().getFirst().label());
		assertEquals(2, response.rounds().getFirst().matches().size());
		assertEquals(2, savedMatches.size());
		assertKnockoutMatch(savedMatches.get(0), 1, "Alfa", "Delta");
		assertKnockoutMatch(savedMatches.get(1), 2, "Beta", "Gamma");
	}

	@Test
	void generaSemifinalesGroupsA1B2YB1A2() {
		Tournament tournament = tournament(TournamentFormat.GROUPS, TournamentStatus.ACTIVE);
		TournamentTeam a1 = tournamentTeam(100L, tournament, team(10L, "A1"), "A");
		TournamentTeam a2 = tournamentTeam(101L, tournament, team(11L, "A2"), "A");
		TournamentTeam b1 = tournamentTeam(102L, tournament, team(12L, "B1"), "B");
		TournamentTeam b2 = tournamentTeam(103L, tournament, team(13L, "B2"), "B");
		List<TournamentTeam> tournamentTeams = List.of(a1, a2, b1, b2);
		List<Match> savedMatches = stubGeneration(
				tournament,
				groupsStandings(a1, a2, b1, b2),
				tournamentTeams
		);

		service.generate(
				1L,
				new KnockoutGenerateRequest(4, 2, Instant.parse("2026-07-01T20:00:00Z"))
		);

		assertEquals(2, savedMatches.size());
		assertKnockoutMatch(savedMatches.get(0), 1, "A1", "B2");
		assertKnockoutMatch(savedMatches.get(1), 2, "B1", "A2");
	}

	@Test
	void rechazaQualifiersCountInvalido() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

		assertThrows(
				BusinessRuleException.class,
				() -> service.generate(
						1L,
						new KnockoutGenerateRequest(6, null, Instant.parse("2026-07-01T20:00:00Z"))
				)
		);
	}

	@Test
	void rechazaGenerarSiYaExisteLlave() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.existsByTournamentIdAndPhase(1L, MatchPhase.KNOCKOUT)).thenReturn(true);

		assertThrows(
				BusinessRuleException.class,
				() -> service.generate(
						1L,
						new KnockoutGenerateRequest(4, null, Instant.parse("2026-07-01T20:00:00Z"))
				)
		);
	}

	@Test
	void rechazaTorneoNoActive() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.DRAFT);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

		assertThrows(
				BusinessRuleException.class,
				() -> service.generate(
						1L,
						new KnockoutGenerateRequest(4, null, Instant.parse("2026-07-01T20:00:00Z"))
				)
		);
	}

	@Test
	void getKnockoutDevuelveRondasAgrupadas() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		Team alfa = team(10L, "Alfa");
		Team beta = team(11L, "Beta");
		Team gamma = team(12L, "Gamma");
		Match semi = knockoutMatch(500L, tournament, alfa, beta, KnockoutRound.SEMI_FINAL, 1);
		semi.setWinnerTeam(alfa);
		Match finalMatch = knockoutMatch(501L, tournament, alfa, gamma, KnockoutRound.FINAL, 1);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByTournamentIdAndPhaseOrderByKnockoutRoundAscBracketPositionAscStartTimeAsc(
				1L,
				MatchPhase.KNOCKOUT
		)).thenReturn(List.of(finalMatch, semi));

		KnockoutBracketResponse response = service.getBracket(1L);

		assertEquals(2, response.rounds().size());
		assertEquals(KnockoutRound.SEMI_FINAL, response.rounds().get(0).round());
		assertEquals(KnockoutRound.FINAL, response.rounds().get(1).round());
		assertEquals(500L, response.rounds().get(0).matches().getFirst().matchId());
		assertEquals(10L, response.rounds().get(0).matches().getFirst().winnerTeamId());
		assertEquals("Alfa", response.rounds().get(0).matches().getFirst().winnerTeamName());
		assertEquals(501L, response.rounds().get(1).matches().getFirst().matchId());
		assertNull(response.rounds().get(1).matches().getFirst().winnerTeamId());
	}

	@Test
	void noAvanzaSiHayPartidoPorJugarse() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		Team alfa = team(10L, "Alfa");
		Team beta = team(11L, "Beta");
		Team gamma = team(12L, "Gamma");
		Team delta = team(13L, "Delta");
		Match semiUno = knockoutMatch(500L, tournament, alfa, beta, KnockoutRound.SEMI_FINAL, 1);
		Match semiDos = finishedKnockoutMatch(501L, tournament, gamma, delta, KnockoutRound.SEMI_FINAL, 2, gamma);
		stubKnockoutMatches(tournament, List.of(semiUno, semiDos));

		assertThrows(
				BusinessRuleException.class,
				() -> service.advance(1L, new KnockoutAdvanceRequest(Instant.parse("2026-07-05T20:00:00Z")))
		);
	}

	@Test
	void noAvanzaSiHayFinalizadoSinWinnerTeam() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		Team alfa = team(10L, "Alfa");
		Team beta = team(11L, "Beta");
		Team gamma = team(12L, "Gamma");
		Team delta = team(13L, "Delta");
		Match semiUno = knockoutMatch(500L, tournament, alfa, beta, KnockoutRound.SEMI_FINAL, 1);
		semiUno.setStatus(MatchStatus.FINALIZADO);
		semiUno.setHomeGoals(2);
		semiUno.setAwayGoals(0);
		Match semiDos = finishedKnockoutMatch(501L, tournament, gamma, delta, KnockoutRound.SEMI_FINAL, 2, gamma);
		stubKnockoutMatches(tournament, List.of(semiUno, semiDos));

		assertThrows(
				BusinessRuleException.class,
				() -> service.advance(1L, new KnockoutAdvanceRequest(Instant.parse("2026-07-05T20:00:00Z")))
		);
	}

	@Test
	void avanzaDeSemifinalAFinalConGanadores() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		Team alfa = team(10L, "Alfa");
		Team beta = team(11L, "Beta");
		Team gamma = team(12L, "Gamma");
		Team delta = team(13L, "Delta");
		Match semiUno = finishedKnockoutMatch(500L, tournament, alfa, beta, KnockoutRound.SEMI_FINAL, 1, alfa);
		Match semiDos = finishedKnockoutMatch(501L, tournament, gamma, delta, KnockoutRound.SEMI_FINAL, 2, delta);
		List<Match> knockoutMatches = new ArrayList<>(List.of(semiUno, semiDos));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByTournamentIdAndPhaseOrderByKnockoutRoundAscBracketPositionAscStartTimeAsc(
				1L,
				MatchPhase.KNOCKOUT
		)).thenAnswer(invocation -> List.copyOf(knockoutMatches));
		when(matchDayRepository.existsByTournamentIdAndNameIgnoreCase(1L, "Final")).thenReturn(false);
		when(matchDayRepository.save(any(MatchDay.class))).thenAnswer(invocation -> {
			MatchDay matchDay = invocation.getArgument(0);
			matchDay.setId(900L);
			return matchDay;
		});
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
			Match match = invocation.getArgument(0);
			match.setId(700L);
			knockoutMatches.add(match);
			return match;
		});

		KnockoutBracketResponse response = service.advance(
				1L,
				new KnockoutAdvanceRequest(Instant.parse("2026-07-05T20:00:00Z"))
		);

		Match finalMatch = knockoutMatches.get(2);
		assertEquals(KnockoutRound.FINAL, finalMatch.getKnockoutRound());
		assertEquals(MatchPhase.KNOCKOUT, finalMatch.getPhase());
		assertEquals(MatchStatus.POR_JUGARSE, finalMatch.getStatus());
		assertEquals(1, finalMatch.getBracketPosition());
		assertEquals("Alfa", finalMatch.getHomeTeam().getName());
		assertEquals("Delta", finalMatch.getAwayTeam().getName());
		assertNull(finalMatch.getWinnerTeam());
		assertEquals(2, response.rounds().size());
		assertEquals(KnockoutRound.FINAL, response.rounds().get(1).round());
		assertEquals(700L, response.rounds().get(1).matches().getFirst().matchId());
	}

	@Test
	void noAvanzaDesdeFinal() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		Team alfa = team(10L, "Alfa");
		Team beta = team(11L, "Beta");
		Match finalMatch = finishedKnockoutMatch(500L, tournament, alfa, beta, KnockoutRound.FINAL, 1, alfa);
		stubKnockoutMatches(tournament, List.of(finalMatch));

		assertThrows(
				BusinessRuleException.class,
				() -> service.advance(1L, new KnockoutAdvanceRequest(Instant.parse("2026-07-05T20:00:00Z")))
		);
	}

	@Test
	void noDuplicaRondaSiguienteSiYaExisteFinal() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE, TournamentStatus.ACTIVE);
		Team alfa = team(10L, "Alfa");
		Team beta = team(11L, "Beta");
		Team gamma = team(12L, "Gamma");
		Team delta = team(13L, "Delta");
		Match semiUno = finishedKnockoutMatch(500L, tournament, alfa, beta, KnockoutRound.SEMI_FINAL, 1, alfa);
		Match semiDos = finishedKnockoutMatch(501L, tournament, gamma, delta, KnockoutRound.SEMI_FINAL, 2, gamma);
		Match finalMatch = knockoutMatch(502L, tournament, alfa, gamma, KnockoutRound.FINAL, 1);
		stubKnockoutMatches(tournament, List.of(semiUno, semiDos, finalMatch));

		assertThrows(
				BusinessRuleException.class,
				() -> service.advance(1L, new KnockoutAdvanceRequest(Instant.parse("2026-07-05T20:00:00Z")))
		);
	}

	private List<Match> stubGeneration(
			Tournament tournament,
			TournamentStandingsResponse standings,
			List<TournamentTeam> tournamentTeams
	) {
		List<Match> savedMatches = new ArrayList<>();
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.existsByTournamentIdAndPhase(1L, MatchPhase.KNOCKOUT)).thenReturn(false);
		when(tournamentStandingService.getStandings(1L)).thenReturn(standings);
		when(matchDayRepository.existsByTournamentIdAndNameIgnoreCase(1L, "Semifinales")).thenReturn(false);
		when(matchDayRepository.save(any(MatchDay.class))).thenAnswer(invocation -> {
			MatchDay matchDay = invocation.getArgument(0);
			matchDay.setId(900L);
			return matchDay;
		});
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
			Match match = invocation.getArgument(0);
			match.setId(500L + savedMatches.size());
			savedMatches.add(match);
			return match;
		});
		when(matchRepository.findByTournamentIdAndPhaseOrderByKnockoutRoundAscBracketPositionAscStartTimeAsc(
				1L,
				MatchPhase.KNOCKOUT
		)).thenAnswer(invocation -> List.copyOf(savedMatches));

		for (TournamentTeam tournamentTeam : tournamentTeams) {
			when(tournamentTeamRepository.findByTournamentIdAndTeamId(
					1L,
					tournamentTeam.getTeam().getId()
			)).thenReturn(Optional.of(tournamentTeam));
		}

		return savedMatches;
	}

	private TournamentStandingsResponse leagueStandings(List<TournamentTeam> tournamentTeams) {
		return new TournamentStandingsResponse(
				1L,
				"Torneo",
				TournamentFormat.LEAGUE,
				List.of(new StandingsGroupResponse(
						null,
						List.of(
								row(1, tournamentTeams.get(0), null, 9, 4, 10),
								row(2, tournamentTeams.get(1), null, 7, 2, 8),
								row(3, tournamentTeams.get(2), null, 5, 1, 6),
								row(4, tournamentTeams.get(3), null, 4, 0, 5)
						)
				))
		);
	}

	private TournamentStandingsResponse groupsStandings(
			TournamentTeam a1,
			TournamentTeam a2,
			TournamentTeam b1,
			TournamentTeam b2
	) {
		return new TournamentStandingsResponse(
				1L,
				"Torneo",
				TournamentFormat.GROUPS,
				List.of(
						new StandingsGroupResponse("A", List.of(
								row(1, a1, "A", 9, 4, 10),
								row(2, a2, "A", 6, 1, 5)
						)),
						new StandingsGroupResponse("B", List.of(
								row(1, b1, "B", 8, 3, 9),
								row(2, b2, "B", 5, 0, 4)
						))
				)
		);
	}

	private StandingRowResponse row(
			int position,
			TournamentTeam tournamentTeam,
			String groupName,
			int points,
			int goalDifference,
			int goalsFor
	) {
		return new StandingRowResponse(
				position,
				tournamentTeam.getTeam().getId(),
				tournamentTeam.getTeam().getName(),
				groupName,
				3,
				2,
				0,
				1,
				goalsFor,
				goalsFor - goalDifference,
				goalDifference,
				points
		);
	}

	private void assertKnockoutMatch(Match match, int bracketPosition, String homeName, String awayName) {
		assertEquals(MatchPhase.KNOCKOUT, match.getPhase());
		assertEquals(MatchStatus.POR_JUGARSE, match.getStatus());
		assertEquals(KnockoutRound.SEMI_FINAL, match.getKnockoutRound());
		assertEquals(bracketPosition, match.getBracketPosition());
		assertEquals(homeName, match.getHomeTeam().getName());
		assertEquals(awayName, match.getAwayTeam().getName());
	}

	private Match knockoutMatch(
			Long id,
			Tournament tournament,
			Team homeTeam,
			Team awayTeam,
			KnockoutRound round,
			Integer bracketPosition
	) {
		MatchDay matchDay = new MatchDay("Ronda", tournament, null);
		Match match = new Match(tournament, matchDay, homeTeam, awayTeam, Instant.parse("2026-07-01T20:00:00Z"));
		match.setId(id);
		match.setStatus(MatchStatus.POR_JUGARSE);
		match.setPhase(MatchPhase.KNOCKOUT);
		match.setKnockoutRound(round);
		match.setBracketPosition(bracketPosition);
		return match;
	}

	private Match finishedKnockoutMatch(
			Long id,
			Tournament tournament,
			Team homeTeam,
			Team awayTeam,
			KnockoutRound round,
			Integer bracketPosition,
			Team winnerTeam
	) {
		Match match = knockoutMatch(id, tournament, homeTeam, awayTeam, round, bracketPosition);
		match.setStatus(MatchStatus.FINALIZADO);
		match.setHomeGoals(winnerTeam == homeTeam ? 2 : 0);
		match.setAwayGoals(winnerTeam == awayTeam ? 2 : 0);
		match.setWinnerTeam(winnerTeam);
		return match;
	}

	private void stubKnockoutMatches(Tournament tournament, List<Match> matches) {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByTournamentIdAndPhaseOrderByKnockoutRoundAscBracketPositionAscStartTimeAsc(
				1L,
				MatchPhase.KNOCKOUT
		)).thenReturn(matches);
	}

	private Tournament tournament(TournamentFormat format, TournamentStatus status) {
		Tournament tournament = new Tournament("Torneo", null, format);
		tournament.setId(1L);
		tournament.setStatus(status);
		return tournament;
	}

	private List<TournamentTeam> tournamentTeams(Tournament tournament, String... names) {
		List<TournamentTeam> tournamentTeams = new ArrayList<>();

		for (int index = 0; index < names.length; index++) {
			tournamentTeams.add(tournamentTeam(
					100L + index,
					tournament,
					team(10L + index, names[index]),
					null
			));
		}

		return tournamentTeams;
	}

	private TournamentTeam tournamentTeam(Long id, Tournament tournament, Team team, String groupName) {
		TournamentTeam tournamentTeam = new TournamentTeam(tournament, team, groupName);
		tournamentTeam.setId(id);
		return tournamentTeam;
	}

	private Team team(Long id, String name) {
		Team team = new Team(name);
		team.setId(id);
		return team;
	}
}
