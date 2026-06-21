package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchPhase;
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
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchBulkItemRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchResultRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TournamentMatchServiceTest {

	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final MatchDayRepository matchDayRepository = mock(MatchDayRepository.class);
	private final MatchRepository matchRepository = mock(MatchRepository.class);
	private final TournamentTeamRepository tournamentTeamRepository = mock(TournamentTeamRepository.class);
	private final PredictionRepository predictionRepository = mock(PredictionRepository.class);
	private final PredictionScoringService predictionScoringService = mock(PredictionScoringService.class);
	private final MatchDayService matchDayService = mock(MatchDayService.class);
	private final TournamentMatchService service = new TournamentMatchService(
			tournamentRepository,
			matchDayRepository,
			matchRepository,
			tournamentTeamRepository,
			predictionRepository,
			predictionScoringService,
			matchDayService
	);

	@Test
	void creaPartidoCorrectamente() {
		TestData data = baseData();
		mockBaseLookups(data);
		when(matchRepository.existsDuplicatedTeamsInMatchDay(100L, 10L, 11L)).thenReturn(false);
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> savedMatch(invocation.getArgument(0), 500L));

		TournamentMatchResponse response = service.create(
				1L,
				new TournamentMatchCreateRequest(100L, 200L, 201L, Instant.parse("2026-06-20T19:00:00Z"))
		);

		assertEquals(500L, response.id());
		assertEquals(200L, response.homeTournamentTeamId());
		assertEquals(201L, response.awayTournamentTeamId());
		assertEquals(MatchStatus.POR_JUGARSE, response.status());
		assertEquals(MatchPhase.REGULAR, response.phase());
		assertNull(response.knockoutRound());
		assertNull(response.bracketPosition());
	}

	@Test
	void noPermiteUsarEquipoDeOtroTorneo() {
		TestData data = baseData();
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchDayRepository.findByIdAndTournamentId(100L, 1L)).thenReturn(Optional.of(data.matchDay()));
		when(tournamentTeamRepository.findByIdAndTournamentIdWithTeam(200L, 1L)).thenReturn(Optional.of(data.homeTournamentTeam()));
		when(tournamentTeamRepository.findByIdAndTournamentIdWithTeam(999L, 1L)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> service.create(1L, new TournamentMatchCreateRequest(100L, 200L, 999L, Instant.parse("2026-06-20T19:00:00Z")))
		);
	}

	@Test
	void noPermiteLocalIgualAVisitante() {
		TestData data = baseDataSameTeam();
		mockBaseLookups(data);

		assertThrows(
				BusinessRuleException.class,
				() -> service.create(1L, new TournamentMatchCreateRequest(100L, 200L, 201L, Instant.parse("2026-06-20T19:00:00Z")))
		);
	}

	@Test
	void noPermitePartidoDuplicadoOInvertidoEnLaMismaFecha() {
		TestData data = baseData();
		mockBaseLookups(data);
		when(matchRepository.existsDuplicatedTeamsInMatchDay(100L, 10L, 11L)).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> service.create(1L, new TournamentMatchCreateRequest(100L, 200L, 201L, Instant.parse("2026-06-20T19:00:00Z")))
		);
	}

	@Test
	void noPermiteUsarFechaDeOtroTorneo() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(1L)));
		when(matchDayRepository.findByIdAndTournamentId(100L, 1L)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> service.create(1L, new TournamentMatchCreateRequest(100L, 200L, 201L, Instant.parse("2026-06-20T19:00:00Z")))
		);
	}

	@Test
	void creaPartidosConBulk() {
		TestData data = baseData();
		AtomicLong ids = new AtomicLong(500L);
		mockBaseLookups(data);
		when(matchRepository.existsDuplicatedTeamsInMatchDay(100L, 10L, 11L)).thenReturn(false);
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> savedMatch(invocation.getArgument(0), ids.getAndIncrement()));

		List<TournamentMatchResponse> response = service.createBulk(
				1L,
				new TournamentMatchBulkCreateRequest(100L, List.of(
						new TournamentMatchBulkItemRequest(200L, 201L, Instant.parse("2026-06-20T19:00:00Z"))
				))
		);

		assertEquals(1, response.size());
	}

	@Test
	void listaPorTorneo() {
		TestData data = baseData();
		Match match = match(500L, data);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByTournamentIdOrderByStartTimeAsc(1L)).thenReturn(List.of(match));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 10L)).thenReturn(Optional.of(data.homeTournamentTeam()));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 11L)).thenReturn(Optional.of(data.awayTournamentTeam()));

		assertEquals(1, service.findAll(1L).size());
	}

	@Test
	void listaPorFecha() {
		TestData data = baseData();
		Match match = match(500L, data);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchDayRepository.findByIdAndTournamentId(100L, 1L)).thenReturn(Optional.of(data.matchDay()));
		when(matchRepository.findByTournamentIdAndMatchDayIdOrderByStartTimeAsc(1L, 100L)).thenReturn(List.of(match));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 10L)).thenReturn(Optional.of(data.homeTournamentTeam()));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 11L)).thenReturn(Optional.of(data.awayTournamentTeam()));

		assertEquals(1, service.findByMatchDay(1L, 100L).size());
	}

	@Test
	void eliminaPartidoSinDependencias() {
		TestData data = baseData();
		Match match = match(500L, data);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByIdAndTournamentId(500L, 1L)).thenReturn(Optional.of(match));
		when(predictionRepository.existsByMatchId(500L)).thenReturn(false);

		service.remove(1L, 500L);

		verify(matchRepository).delete(match);
	}

	@Test
	void noEliminaPartidoConPronosticos() {
		TestData data = baseData();
		Match match = match(500L, data);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByIdAndTournamentId(500L, 1L)).thenReturn(Optional.of(match));
		when(predictionRepository.existsByMatchId(500L)).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> service.remove(1L, 500L));
	}

	@Test
	void noEliminaPartidoConResultado() {
		TestData data = baseData();
		Match match = match(500L, data);
		match.setHomeGoals(1);
		match.setAwayGoals(0);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByIdAndTournamentId(500L, 1L)).thenReturn(Optional.of(match));

		assertThrows(BusinessRuleException.class, () -> service.remove(1L, 500L));
	}

	@Test
	void adminCargaResultadoCorrectamenteYFinalizaPartido() {
		TestData data = baseData();
		data.tournament().setStatus(TournamentStatus.ACTIVE);
		Match match = match(500L, data);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByIdAndTournamentId(500L, 1L)).thenReturn(Optional.of(match));
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 10L)).thenReturn(Optional.of(data.homeTournamentTeam()));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 11L)).thenReturn(Optional.of(data.awayTournamentTeam()));

		TournamentMatchResponse response = service.saveResult(
				1L,
				500L,
				new TournamentMatchResultRequest(3, 0)
		);

		assertEquals(MatchStatus.FINALIZADO, response.status());
		assertEquals(3, response.homeGoals());
		assertEquals(0, response.awayGoals());
		assertEquals(ResultTrend.LOCAL, response.resultTrend());
		verify(predictionScoringService).scoreMatchPredictions(match);
		verify(matchDayService).refreshStatusByMatchDayId(100L);
	}

	@Test
	void corregirResultadoNoDuplicaPuntosPorqueRecalculaElPartido() {
		TestData data = baseData();
		data.tournament().setStatus(TournamentStatus.FINISHED);
		Match match = match(500L, data);
		match.setStatus(MatchStatus.FINALIZADO);
		match.setHomeGoals(1);
		match.setAwayGoals(0);
		match.setResultTrend(ResultTrend.LOCAL);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByIdAndTournamentId(500L, 1L)).thenReturn(Optional.of(match));
		when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 10L)).thenReturn(Optional.of(data.homeTournamentTeam()));
		when(tournamentTeamRepository.findByTournamentIdAndTeamId(1L, 11L)).thenReturn(Optional.of(data.awayTournamentTeam()));

		TournamentMatchResponse response = service.saveResult(
				1L,
				500L,
				new TournamentMatchResultRequest(0, 2)
		);

		assertEquals(0, response.homeGoals());
		assertEquals(2, response.awayGoals());
		assertEquals(ResultTrend.VISITANTE, response.resultTrend());
		verify(predictionScoringService).scoreMatchPredictions(match);
	}

	@Test
	void noPermiteCargarResultadoEnTorneoDraft() {
		TestData data = baseData();
		data.tournament().setStatus(TournamentStatus.DRAFT);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));

		assertThrows(
				BusinessRuleException.class,
				() -> service.saveResult(1L, 500L, new TournamentMatchResultRequest(1, 0))
		);
	}

	@Test
	void noPermiteCargarResultadoDePartidoDeOtroTorneo() {
		TestData data = baseData();
		data.tournament().setStatus(TournamentStatus.ACTIVE);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchRepository.findByIdAndTournamentId(999L, 1L)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> service.saveResult(1L, 999L, new TournamentMatchResultRequest(1, 0))
		);
	}

	private void mockBaseLookups(TestData data) {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(data.tournament()));
		when(matchDayRepository.findByIdAndTournamentId(100L, 1L)).thenReturn(Optional.of(data.matchDay()));
		when(tournamentTeamRepository.findByIdAndTournamentIdWithTeam(200L, 1L)).thenReturn(Optional.of(data.homeTournamentTeam()));
		when(tournamentTeamRepository.findByIdAndTournamentIdWithTeam(201L, 1L)).thenReturn(Optional.of(data.awayTournamentTeam()));
	}

	private TestData baseData() {
		Tournament tournament = tournament(1L);
		MatchDay matchDay = matchDay(100L, tournament);
		Team homeTeam = team(10L, "Argentina");
		Team awayTeam = team(11L, "Argelia");
		TournamentTeam homeTournamentTeam = tournamentTeam(200L, tournament, homeTeam, "J");
		TournamentTeam awayTournamentTeam = tournamentTeam(201L, tournament, awayTeam, "J");
		return new TestData(tournament, matchDay, homeTournamentTeam, awayTournamentTeam);
	}

	private TestData baseDataSameTeam() {
		Tournament tournament = tournament(1L);
		MatchDay matchDay = matchDay(100L, tournament);
		Team team = team(10L, "Argentina");
		TournamentTeam homeTournamentTeam = tournamentTeam(200L, tournament, team, "J");
		TournamentTeam awayTournamentTeam = tournamentTeam(201L, tournament, team, "J");
		return new TestData(tournament, matchDay, homeTournamentTeam, awayTournamentTeam);
	}

	private Tournament tournament(Long id) {
		Tournament tournament = new Tournament("Torneo", null, TournamentFormat.GROUPS);
		tournament.setId(id);
		tournament.setStatus(TournamentStatus.DRAFT);
		tournament.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		tournament.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return tournament;
	}

	private MatchDay matchDay(Long id, Tournament tournament) {
		MatchDay matchDay = new MatchDay("Fecha 1", tournament, 1);
		matchDay.setId(id);
		matchDay.setStatus(MatchDayStatus.PROGRAMADA);
		matchDay.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		matchDay.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return matchDay;
	}

	private Team team(Long id, String name) {
		Team team = new Team(name);
		team.setId(id);
		team.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return team;
	}

	private TournamentTeam tournamentTeam(Long id, Tournament tournament, Team team, String groupName) {
		TournamentTeam tournamentTeam = new TournamentTeam(tournament, team, groupName);
		tournamentTeam.setId(id);
		tournamentTeam.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		tournamentTeam.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return tournamentTeam;
	}

	private Match match(Long id, TestData data) {
		Match match = new Match(
				data.tournament(),
				data.matchDay(),
				data.homeTournamentTeam().getTeam(),
				data.awayTournamentTeam().getTeam(),
				Instant.parse("2026-06-20T19:00:00Z")
		);
		match.setId(id);
		match.setStatus(MatchStatus.POR_JUGARSE);
		match.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		match.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return match;
	}

	private Match savedMatch(Match match, Long id) {
		match.setId(id);
		match.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		match.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return match;
	}

	private record TestData(
			Tournament tournament,
			MatchDay matchDay,
			TournamentTeam homeTournamentTeam,
			TournamentTeam awayTournamentTeam
	) {
	}
}
