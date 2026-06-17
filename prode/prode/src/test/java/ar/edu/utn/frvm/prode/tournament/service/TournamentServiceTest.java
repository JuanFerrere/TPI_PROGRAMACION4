package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStatusUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del service de torneos.
 *
 * No levanta Spring ni base de datos. Usa un mock de TournamentRepository para
 * validar la logica propia del modulo nuevo.
 */
class TournamentServiceTest {

	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final TournamentService tournamentService = new TournamentService(tournamentRepository);

	@Test
	void creaTorneoConNombreNormalizadoYEstadoDraft() {
		Tournament savedTournament = tournament(1L, "Mundial 2026", "Prode del Mundial 2026", TournamentStatus.DRAFT);
		when(tournamentRepository.existsByNameIgnoreCase("Mundial 2026")).thenReturn(false);
		when(tournamentRepository.save(any(Tournament.class))).thenReturn(savedTournament);

		TournamentResponse response = tournamentService.create(
				new TournamentCreateRequest("  Mundial 2026  ", "Prode del Mundial 2026")
		);

		assertEquals(1L, response.id());
		assertEquals("Mundial 2026", response.name());
		assertEquals(TournamentStatus.DRAFT, response.status());
		verify(tournamentRepository).existsByNameIgnoreCase("Mundial 2026");
	}

	@Test
	void noPermiteNombreDuplicadoIgnorandoMayusculas() {
		when(tournamentRepository.existsByNameIgnoreCase("Mundial 2026")).thenReturn(true);

		assertThrows(
				DuplicateResourceException.class,
				() -> tournamentService.create(new TournamentCreateRequest("Mundial 2026", null))
		);
	}

	@Test
	void consultaPorIdInexistenteDevuelveResourceNotFound() {
		when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> tournamentService.findById(99L));
	}

	@Test
	void actualizaEstadoDelTorneo() {
		Tournament tournament = tournament(1L, "Champions League", null, TournamentStatus.DRAFT);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

		TournamentResponse response = tournamentService.updateStatus(
				1L,
				new TournamentStatusUpdateRequest("ACTIVE")
		);

		assertEquals(TournamentStatus.ACTIVE, response.status());
		assertEquals(TournamentStatus.ACTIVE, tournament.getStatus());
	}

	@Test
	void noPermiteActualizarConEstadoInvalido() {
		Tournament tournament = tournament(1L, "Champions League", null, TournamentStatus.DRAFT);
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

		assertThrows(
				BusinessRuleException.class,
				() -> tournamentService.updateStatus(1L, new TournamentStatusUpdateRequest("PAUSADO"))
		);
	}

	private Tournament tournament(
			Long id,
			String name,
			String description,
			TournamentStatus status
	) {
		Tournament tournament = new Tournament(name, description);
		tournament.setId(id);
		tournament.setStatus(status);
		tournament.setCreatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		tournament.setUpdatedAt(Instant.parse("2026-06-17T00:00:00Z"));
		return tournament;
	}
}
