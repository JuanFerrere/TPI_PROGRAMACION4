package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchPhase;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.tournament.dto.StandingsGroupResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStandingsResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TournamentStandingServiceTest {

	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final TournamentTeamRepository tournamentTeamRepository = mock(TournamentTeamRepository.class);
	private final MatchRepository matchRepository = mock(MatchRepository.class);
	private final TournamentStandingService standingService = new TournamentStandingService(
			tournamentRepository, tournamentTeamRepository, matchRepository
	);

	@Test
	void calculaPuntosResultadosYOrdenaLaTabla() {
		Tournament tournament = tournament(1L, TournamentFormat.LEAGUE);
		Team a = team(10L, "Alpha");
		Team b = team(11L, "Beta");
		Team c = team(12L, "Gamma");
		stubTeams(tournament, tournamentTeam(tournament, a, null), tournamentTeam(tournament, b, null), tournamentTeam(tournament, c, null));
		stubFinishedMatches(
				finishedMatch(tournament, a, b, 2, 0),
				finishedMatch(tournament, a, c, 1, 1),
				finishedMatch(tournament, b, c, 3, 1)
		);

		List<StandingsGroupResponse> groups = standingService.getStandings(1L).groups();

		assertEquals(1, groups.size());
		var rows = groups.getFirst().rows();
		assertEquals("Alpha", rows.get(0).teamName());
		assertEquals(4, rows.get(0).points());
		assertEquals(2, rows.get(0).played());
		assertEquals(1, rows.get(0).won());
		assertEquals(1, rows.get(0).drawn());
		assertEquals(2, rows.get(0).goalDifference());
		assertEquals("Beta", rows.get(1).teamName());
		assertEquals(3, rows.get(1).points());
		assertEquals("Gamma", rows.get(2).teamName());
		assertEquals(1, rows.get(2).points());
		assertEquals(1, rows.get(0).position());
		assertEquals(3, rows.get(2).position());
	}

	@Test
	void equipoSinPartidosApareceEnCero() {
		Tournament tournament = tournament(1L, TournamentFormat.LEAGUE);
		Team a = team(10L, "Alpha");
		Team sinJugar = team(99L, "Zeta");
		stubTeams(tournament, tournamentTeam(tournament, a, null), tournamentTeam(tournament, sinJugar, null));
		stubFinishedMatches();

		var rows = standingService.getStandings(1L).groups().getFirst().rows();

		assertEquals(2, rows.size());
		assertEquals(0, rows.get(0).played());
		assertEquals(0, rows.get(0).points());
	}

	@Test
	void cuentaPartidosConPhaseRegular() {
		Tournament tournament = tournament(1L, TournamentFormat.LEAGUE);
		Team a = team(10L, "Alpha");
		Team b = team(11L, "Beta");
		stubTeams(tournament, tournamentTeam(tournament, a, null), tournamentTeam(tournament, b, null));
		stubFinishedMatches(finishedMatch(tournament, a, b, 2, 0, MatchPhase.REGULAR));

		var rows = standingService.getStandings(1L).groups().getFirst().rows();

		assertEquals("Alpha", rows.get(0).teamName());
		assertEquals(3, rows.get(0).points());
		assertEquals(1, rows.get(0).played());
	}

	@Test
	void cuentaPartidosConPhaseNullComoRegular() {
		Tournament tournament = tournament(1L, TournamentFormat.LEAGUE);
		Team a = team(10L, "Alpha");
		Team b = team(11L, "Beta");
		stubTeams(tournament, tournamentTeam(tournament, a, null), tournamentTeam(tournament, b, null));
		stubFinishedMatches(finishedMatch(tournament, a, b, 1, 0));

		var rows = standingService.getStandings(1L).groups().getFirst().rows();

		assertEquals("Alpha", rows.get(0).teamName());
		assertEquals(3, rows.get(0).points());
		assertEquals(1, rows.get(0).played());
	}

	@Test
	void noCuentaPartidosKnockoutEnLaTablaRegular() {
		Tournament tournament = tournament(1L, TournamentFormat.LEAGUE);
		Team a = team(10L, "Alpha");
		Team b = team(11L, "Beta");
		stubTeams(tournament, tournamentTeam(tournament, a, null), tournamentTeam(tournament, b, null));
		stubFinishedMatches(
				finishedMatch(tournament, a, b, 1, 0, MatchPhase.REGULAR),
				finishedMatch(tournament, b, a, 8, 0, MatchPhase.KNOCKOUT)
		);

		var rows = standingService.getStandings(1L).groups().getFirst().rows();

		assertEquals("Alpha", rows.get(0).teamName());
		assertEquals(3, rows.get(0).points());
		assertEquals(1, rows.get(0).played());
		assertEquals(1, rows.get(0).goalsFor());
		assertEquals("Beta", rows.get(1).teamName());
		assertEquals(0, rows.get(1).points());
		assertEquals(1, rows.get(1).played());
		assertEquals(0, rows.get(1).goalsFor());
	}

	@Test
	void desempataPorDiferenciaDeGol() {
		Tournament tournament = tournament(1L, TournamentFormat.LEAGUE);
		Team x = team(10L, "Equis");
		Team y = team(11L, "Ye");
		Team z = team(12L, "Zeta");
		stubTeams(tournament, tournamentTeam(tournament, x, null), tournamentTeam(tournament, y, null), tournamentTeam(tournament, z, null));
		stubFinishedMatches(
				finishedMatch(tournament, x, z, 3, 0),
				finishedMatch(tournament, y, z, 1, 0)
		);

		var rows = standingService.getStandings(1L).groups().getFirst().rows();

		assertEquals(3, rows.get(0).points());
		assertEquals(3, rows.get(1).points());
		assertEquals("Equis", rows.get(0).teamName());
		assertEquals(3, rows.get(0).goalDifference());
		assertEquals("Ye", rows.get(1).teamName());
	}

	@Test
	void formatoGroupsDevuelveUnaTablaPorGrupo() {
		Tournament tournament = tournament(1L, TournamentFormat.GROUPS);
		Team a1 = team(10L, "A1");
		Team a2 = team(11L, "A2");
		Team b1 = team(12L, "B1");
		stubTeams(tournament,
				tournamentTeam(tournament, a1, "A"),
				tournamentTeam(tournament, a2, "A"),
				tournamentTeam(tournament, b1, "B"));
		stubFinishedMatches(finishedMatch(tournament, a1, a2, 2, 0));

		TournamentStandingsResponse response = standingService.getStandings(1L);

		assertEquals(2, response.groups().size());
		StandingsGroupResponse groupA = response.groups().stream()
				.filter(group -> "A".equals(group.groupName())).findFirst().orElseThrow();
		StandingsGroupResponse groupB = response.groups().stream()
				.filter(group -> "B".equals(group.groupName())).findFirst().orElseThrow();
		assertEquals(2, groupA.rows().size());
		assertEquals("A1", groupA.rows().get(0).teamName());
		assertEquals(1, groupB.rows().size());
	}

	@Test
	void torneoInexistenteLanzaResourceNotFound() {
		when(tournamentRepository.findById(404L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> standingService.getStandings(404L));
	}

	private void stubTeams(Tournament tournament, TournamentTeam... teams) {
		when(tournamentRepository.findById(tournament.getId())).thenReturn(Optional.of(tournament));
		when(tournamentTeamRepository.findByTournamentIdOrdered(tournament.getId())).thenReturn(List.of(teams));
	}

	private void stubFinishedMatches(Match... matches) {
		when(matchRepository.findByTournamentIdAndStatusOrderByStartTimeAsc(1L, MatchStatus.FINALIZADO))
				.thenReturn(List.of(matches));
	}

	private Tournament tournament(Long id, TournamentFormat format) {
		Tournament tournament = new Tournament("Torneo " + id, null, format);
		tournament.setId(id);
		return tournament;
	}

	private Team team(Long id, String name) {
		Team team = new Team(name);
		team.setId(id);
		return team;
	}

	private TournamentTeam tournamentTeam(Tournament tournament, Team team, String groupName) {
		return new TournamentTeam(tournament, team, groupName);
	}

	private Match finishedMatch(Tournament tournament, Team home, Team away, int homeGoals, int awayGoals) {
		return finishedMatch(tournament, home, away, homeGoals, awayGoals, null);
	}

	private Match finishedMatch(
			Tournament tournament,
			Team home,
			Team away,
			int homeGoals,
			int awayGoals,
			MatchPhase phase
	) {
		MatchDay matchDay = new MatchDay("Fecha 1", tournament, 1);
		Match match = new Match(tournament, matchDay, home, away, Instant.parse("2026-06-20T19:00:00Z"));
		match.setHomeGoals(homeGoals);
		match.setAwayGoals(awayGoals);
		match.setStatus(MatchStatus.FINALIZADO);
		match.setPhase(phase);
		return match;
	}
}
