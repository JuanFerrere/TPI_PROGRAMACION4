package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.team.repository.TeamRepository;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamGroupUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentTeamResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios de equipos dentro de torneos.
 */
class TournamentTeamServiceTest {

	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final TournamentTeamRepository tournamentTeamRepository = mock(TournamentTeamRepository.class);
	private final TeamRepository teamRepository = mock(TeamRepository.class);
	private final TournamentTeamService service = new TournamentTeamService(
			tournamentRepository,
			tournamentTeamRepository,
			teamRepository
	);

	@Test
	void torneoPorGruposExigeGrupoYLoGuardaEnMayusculas() {
		Tournament tournament = tournament(TournamentFormat.GROUPS);
		Team team = team(10L, "Argentina");
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(teamRepository.findByNameIgnoreCase("Argentina")).thenReturn(Optional.of(team));
		when(tournamentTeamRepository.existsByTournamentIdAndTeamId(1L, 10L)).thenReturn(false);
		when(tournamentTeamRepository.save(any(TournamentTeam.class))).thenAnswer(invocation -> {
			TournamentTeam tournamentTeam = invocation.getArgument(0);
			tournamentTeam.setId(20L);
			tournamentTeam.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
			tournamentTeam.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
			return tournamentTeam;
		});

		TournamentTeamResponse response = service.addTeam(
				1L,
				new TournamentTeamCreateRequest("Argentina", "j")
		);

		assertEquals("J", response.groupName());
	}

	@Test
	void torneoPorGruposNoPermiteEquipoSinGrupo() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(TournamentFormat.GROUPS)));

		assertThrows(
				BusinessRuleException.class,
				() -> service.addTeam(1L, new TournamentTeamCreateRequest("Argentina", null))
		);
	}

	@Test
	void torneoLigaNoGuardaGrupo() {
		Tournament tournament = tournament(TournamentFormat.LEAGUE);
		Team team = team(10L, "River Plate");
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(teamRepository.findByNameIgnoreCase("River Plate")).thenReturn(Optional.of(team));
		when(tournamentTeamRepository.existsByTournamentIdAndTeamId(1L, 10L)).thenReturn(false);
		when(tournamentTeamRepository.save(any(TournamentTeam.class))).thenAnswer(invocation -> {
			TournamentTeam tournamentTeam = invocation.getArgument(0);
			tournamentTeam.setId(20L);
			tournamentTeam.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
			tournamentTeam.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
			return tournamentTeam;
		});

		TournamentTeamResponse response = service.addTeam(
				1L,
				new TournamentTeamCreateRequest("River Plate", null)
		);

		assertNull(response.groupName());
	}

	@Test
	void torneoLigaRechazaGrupoEnCargaMasiva() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(TournamentFormat.LEAGUE)));

		assertThrows(
				BusinessRuleException.class,
				() -> service.addTeamsBulk(1L, new TournamentTeamBulkCreateRequest("River Plate|A"))
		);
	}

	@Test
	void cambiaGrupoSoloEnTorneoPorGrupos() {
		Tournament tournament = tournament(TournamentFormat.GROUPS);
		Team team = team(10L, "Argentina");
		TournamentTeam tournamentTeam = new TournamentTeam(tournament, team, "J");
		tournamentTeam.setId(20L);
		tournamentTeam.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		tournamentTeam.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(tournamentTeamRepository.findByIdAndTournamentId(20L, 1L)).thenReturn(Optional.of(tournamentTeam));

		TournamentTeamResponse response = service.updateGroup(
				1L,
				20L,
				new TournamentTeamGroupUpdateRequest("k")
		);

		assertEquals("K", response.groupName());
	}

	@Test
	void listaEquiposDelTorneoOrdenadosPorRepositorio() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(TournamentFormat.LEAGUE)));
		when(tournamentTeamRepository.findByTournamentIdOrdered(1L)).thenReturn(List.of());

		assertEquals(0, service.findByTournament(1L).size());
	}

	private Tournament tournament(TournamentFormat format) {
		Tournament tournament = new Tournament("Torneo", null, format);
		tournament.setId(1L);
		tournament.setStatus(TournamentStatus.DRAFT);
		tournament.setFormat(format);
		tournament.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		tournament.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return tournament;
	}

	private Team team(Long id, String name) {
		Team team = new Team(name);
		team.setId(id);
		team.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return team;
	}
}
