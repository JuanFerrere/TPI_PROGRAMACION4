package ar.edu.utn.frvm.prode.prediction.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.prediction.dto.PredictionUpsertRequest;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentPredictionResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.user.entity.Role;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PredictionServiceTournamentTest {

	private final PredictionRepository predictionRepository = mock(PredictionRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final MatchRepository matchRepository = mock(MatchRepository.class);
	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final PredictionService service = new PredictionService(
			predictionRepository,
			userRepository,
			matchRepository,
			tournamentRepository
	);

	@Test
	void creaPronosticoDentroDelTorneoCorrecto() {
		User user = user();
		Tournament tournament = tournament(TournamentStatus.ACTIVE);
		Match match = match(tournament, MatchStatus.POR_JUGARSE, Instant.now().plus(2, ChronoUnit.HOURS));
		when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByIdAndTournamentId(10L, 1L)).thenReturn(Optional.of(match));
		when(predictionRepository.findByUserIdAndMatchId(4L, 10L)).thenReturn(Optional.empty());
		when(predictionRepository.save(any(Prediction.class))).thenAnswer(invocation -> savedPrediction(invocation.getArgument(0)));

		TournamentPredictionResponse response = service.upsertTournamentPrediction(
				1L,
				10L,
				new PredictionUpsertRequest(2, 1),
				auth()
		);

		assertEquals(1L, response.tournamentId());
		assertEquals(10L, response.matchId());
		assertEquals(ResultTrend.LOCAL, response.predictedTrend());
	}

	@Test
	void rechazaMatchDeOtroTorneo() {
		when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user()));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(TournamentStatus.ACTIVE)));
		when(matchRepository.findByIdAndTournamentId(99L, 1L)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> service.upsertTournamentPrediction(1L, 99L, new PredictionUpsertRequest(1, 0), auth())
		);
	}

	@Test
	void listaMisPronosticosSoloDelTorneo() {
		User user = user();
		Tournament tournament = tournament(TournamentStatus.ACTIVE);
		Match match = match(tournament, MatchStatus.FINALIZADO, Instant.now().minus(1, ChronoUnit.DAYS));
		Prediction prediction = new Prediction(user, match);
		prediction.updatePrediction(1, 0, ResultTrend.LOCAL);
		prediction.setPoints(3);
		prediction.setExactHit(true);
		when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(predictionRepository.findByUserIdAndMatchTournamentIdOrderByMatchStartTimeAsc(4L, 1L))
				.thenReturn(List.of(prediction));

		List<TournamentPredictionResponse> response = service.getMyTournamentPredictions(1L, auth(), null);

		assertEquals(1, response.size());
		assertEquals(1L, response.getFirst().tournamentId());
	}

	@Test
	void impidePronosticoEnTorneoDraft() {
		when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user()));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(TournamentStatus.DRAFT)));

		assertThrows(
				BusinessRuleException.class,
				() -> service.upsertTournamentPrediction(1L, 10L, new PredictionUpsertRequest(1, 0), auth())
		);
	}

	@Test
	void impidePronosticoEnTorneoFinished() {
		when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user()));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament(TournamentStatus.FINISHED)));

		assertThrows(
				BusinessRuleException.class,
				() -> service.upsertTournamentPrediction(1L, 10L, new PredictionUpsertRequest(1, 0), auth())
		);
	}

	@Test
	void impidePronosticoDespuesDelCierre() {
		User user = user();
		Tournament tournament = tournament(TournamentStatus.ACTIVE);
		Match match = match(tournament, MatchStatus.POR_JUGARSE, Instant.now().plus(10, ChronoUnit.MINUTES));
		when(userRepository.findByUsername("juan")).thenReturn(Optional.of(user));
		when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByIdAndTournamentId(10L, 1L)).thenReturn(Optional.of(match));

		assertThrows(
				BusinessRuleException.class,
				() -> service.upsertTournamentPrediction(1L, 10L, new PredictionUpsertRequest(1, 0), auth())
		);
	}

	private Authentication auth() {
		return new UsernamePasswordAuthenticationToken("juan", null, List.of());
	}

	private User user() {
		User user = new User("juan", "juan@mail.com", "hash", Role.USER);
		user.setId(4L);
		return user;
	}

	private Tournament tournament(TournamentStatus status) {
		Tournament tournament = new Tournament("Mundial 2026", null, TournamentFormat.GROUPS);
		tournament.setId(1L);
		tournament.setStatus(status);
		return tournament;
	}

	private Match match(Tournament tournament, MatchStatus status, Instant startTime) {
		Team home = new Team("Argentina");
		home.setId(20L);
		Team away = new Team("Argelia");
		away.setId(21L);
		MatchDay matchDay = new MatchDay("Fecha 1", tournament, 1);
		matchDay.setId(30L);
		Match match = new Match(tournament, matchDay, home, away, startTime);
		match.setId(10L);
		match.setStatus(status);
		return match;
	}

	private Prediction savedPrediction(Prediction prediction) {
		prediction.setId(100L);
		prediction.setPoints(0);
		prediction.setExactHit(false);
		return prediction;
	}
}
