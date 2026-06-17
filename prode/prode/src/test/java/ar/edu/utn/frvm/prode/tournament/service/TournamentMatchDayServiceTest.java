package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TournamentMatchDayServiceTest {

	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final MatchDayRepository matchDayRepository = mock(MatchDayRepository.class);
	private final MatchRepository matchRepository = mock(MatchRepository.class);
	private final TournamentMatchDayService service = new TournamentMatchDayService(
			tournamentRepository,
			matchDayRepository,
			matchRepository
	);

	@Test
	void creaFechaDentroDelTorneo() {
		Tournament tournament = tournament(1L);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchDayRepository.existsByTournamentIdAndNameIgnoreCase(1L, "Fecha 1")).thenReturn(false);
		when(matchDayRepository.existsByTournamentIdAndOrderNumber(1L, 1)).thenReturn(false);
		when(matchDayRepository.save(any(MatchDay.class))).thenAnswer(invocation -> savedMatchDay(invocation.getArgument(0), 10L));

		TournamentMatchDayResponse response = service.create(
				1L,
				new TournamentMatchDayCreateRequest(" Fecha 1 ", 1)
		);

		assertEquals(10L, response.id());
		assertEquals(1L, response.tournamentId());
		assertEquals("Fecha 1", response.name());
		assertEquals(1, response.orderNumber());
	}

	@Test
	void creaVariasFechasConBulk() {
		AtomicLong ids = new AtomicLong(10L);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(1L)));
		when(matchDayRepository.save(any(MatchDay.class))).thenAnswer(invocation -> savedMatchDay(invocation.getArgument(0), ids.getAndIncrement()));

		List<TournamentMatchDayResponse> response = service.createBulk(
				1L,
				new TournamentMatchDayBulkCreateRequest(List.of(
						new TournamentMatchDayCreateRequest("Fecha 1", 1),
						new TournamentMatchDayCreateRequest("Fecha 2", 2)
				))
		);

		assertEquals(2, response.size());
	}

	@Test
	void noPermiteNombreDuplicadoDentroDelTorneo() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(1L)));
		when(matchDayRepository.existsByTournamentIdAndNameIgnoreCase(1L, "Fecha 1")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> service.create(1L, new TournamentMatchDayCreateRequest("Fecha 1", 1))
		);
	}

	@Test
	void noPermiteOrderNumberDuplicadoDentroDelTorneo() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(1L)));
		when(matchDayRepository.existsByTournamentIdAndOrderNumber(1L, 1)).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> service.create(1L, new TournamentMatchDayCreateRequest("Fecha 1", 1))
		);
	}

	@Test
	void permiteMismoNombreEnTorneosDistintos() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(1L)));
		when(tournamentRepository.findById(2L)).thenReturn(Optional.of(tournament(2L)));
		when(matchDayRepository.save(any(MatchDay.class))).thenAnswer(invocation -> savedMatchDay(invocation.getArgument(0), 10L));

		service.create(1L, new TournamentMatchDayCreateRequest("Fecha 1", 1));
		service.create(2L, new TournamentMatchDayCreateRequest("Fecha 1", 1));

		verify(matchDayRepository).existsByTournamentIdAndNameIgnoreCase(1L, "Fecha 1");
		verify(matchDayRepository).existsByTournamentIdAndNameIgnoreCase(2L, "Fecha 1");
	}

	@Test
	void torneoInexistenteDevuelveResourceNotFound() {
		when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> service.create(99L, new TournamentMatchDayCreateRequest("Fecha 1", 1))
		);
	}

	@Test
	void noEliminaFechaConPartidos() {
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(1L)));
		when(matchDayRepository.findByIdAndTournamentId(10L, 1L)).thenReturn(Optional.of(matchDay(10L, tournament(1L), "Fecha 1", 1)));
		when(matchRepository.existsByMatchDayId(10L)).thenReturn(true);

		assertThrows(BusinessRuleException.class, () -> service.remove(1L, 10L));
	}

	private Tournament tournament(Long id) {
		Tournament tournament = new Tournament("Torneo", null, TournamentFormat.LEAGUE);
		tournament.setId(id);
		tournament.setStatus(TournamentStatus.DRAFT);
		tournament.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		tournament.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return tournament;
	}

	private MatchDay matchDay(Long id, Tournament tournament, String name, Integer orderNumber) {
		MatchDay matchDay = new MatchDay(name, tournament, orderNumber);
		matchDay.setId(id);
		matchDay.setStatus(MatchDayStatus.PROGRAMADA);
		matchDay.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		matchDay.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return matchDay;
	}

	private MatchDay savedMatchDay(MatchDay matchDay, Long id) {
		matchDay.setId(id);
		matchDay.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		matchDay.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return matchDay;
	}
}
