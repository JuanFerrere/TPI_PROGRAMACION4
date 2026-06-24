package ar.edu.utn.frvm.prode.ranking.service;

import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.user.entity.Role;
import ar.edu.utn.frvm.prode.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingServiceTest {

	private final PredictionRepository predictionRepository = mock(PredictionRepository.class);
	private final TournamentRepository tournamentRepository = mock(TournamentRepository.class);
	private final RankingService rankingService = new RankingService(predictionRepository, tournamentRepository);

	@Test
	void rankingIncluyeSolamentePuntosDelTorneo() {
		User ana = user(1L, "ana");
		User beto = user(2L, "beto");
		Tournament tournament = tournament(1L);
		Prediction anaPrediction = prediction(ana, match(tournament), 3, true);
		Prediction betoPrediction = prediction(beto, match(tournament), 1, false);
		when(tournamentRepository.existsById(1L)).thenReturn(true);
		when(predictionRepository.findByMatchTournamentId(1L)).thenReturn(List.of(anaPrediction, betoPrediction));

		List<RankingResponse> ranking = rankingService.getTournamentGlobalRanking(1L);

		assertEquals(2, ranking.size());
		assertEquals("ana", ranking.get(0).username());
		assertEquals(3, ranking.get(0).totalPoints());
	}

	@Test
	void resultadosDeOtroTorneoNoAfectanRanking() {
		User ana = user(1L, "ana");
		Tournament tournament = tournament(1L);
		Prediction prediction = prediction(ana, match(tournament), 1, false);
		when(tournamentRepository.existsById(1L)).thenReturn(true);
		when(predictionRepository.findByMatchTournamentId(1L)).thenReturn(List.of(prediction));

		List<RankingResponse> ranking = rankingService.getTournamentGlobalRanking(1L);

		assertEquals(1, ranking.size());
		assertEquals(1, ranking.get(0).totalPoints());
	}

	@Test
	void correccionDeResultadoActualizaRanking() {
		User ana = user(1L, "ana");
		Tournament tournament = tournament(1L);
		Prediction prediction = prediction(ana, match(tournament), 0, false);
		when(tournamentRepository.existsById(1L)).thenReturn(true);
		when(predictionRepository.findByMatchTournamentId(1L)).thenReturn(List.of(prediction));

		assertEquals(0, rankingService.getTournamentGlobalRanking(1L).getFirst().totalPoints());

		prediction.setPoints(3);
		prediction.setExactHit(true);

		assertEquals(3, rankingService.getTournamentGlobalRanking(1L).getFirst().totalPoints());
		assertEquals(1L, rankingService.getTournamentGlobalRanking(1L).getFirst().exactHits());
	}

	@Test
	void ordenYDesempateConservanReglasActuales() {
		User ana = user(1L, "ana");
		User beto = user(2L, "beto");
		Tournament tournament = tournament(1L);
		Prediction anaPrediction = prediction(ana, match(tournament), 3, false);
		Prediction betoPrediction = prediction(beto, match(tournament), 3, true);
		when(tournamentRepository.existsById(1L)).thenReturn(true);
		when(predictionRepository.findByMatchTournamentId(1L)).thenReturn(List.of(anaPrediction, betoPrediction));

		List<RankingResponse> ranking = rankingService.getTournamentGlobalRanking(1L);

		assertEquals("beto", ranking.getFirst().username());
	}

	@Test
	void rankingExcluyeUsuariosAdminYSostienePuntosDeUser() {
		User user = user(1L, "ana");
		User admin = user(2L, "admin");
		admin.setRole(Role.ADMIN);
		Tournament tournament = tournament(1L);
		Prediction userPrediction = prediction(user, match(tournament), 3, true);
		Prediction adminPrediction = prediction(admin, match(tournament), 9, true);
		when(tournamentRepository.existsById(1L)).thenReturn(true);
		when(predictionRepository.findByMatchTournamentId(1L))
				.thenReturn(List.of(userPrediction, adminPrediction));

		List<RankingResponse> ranking = rankingService.getTournamentGlobalRanking(1L);

		assertEquals(1, ranking.size());
		assertEquals("ana", ranking.getFirst().username());
		assertEquals(3, ranking.getFirst().totalPoints());
		assertEquals(1L, ranking.getFirst().exactHits());
	}

	private User user(Long id, String username) {
		User user = new User(username, username + "@mail.com", "hash", Role.USER);
		user.setId(id);
		return user;
	}

	private Tournament tournament(Long id) {
		Tournament tournament = new Tournament("Torneo " + id, null, TournamentFormat.LEAGUE);
		tournament.setId(id);
		tournament.setStatus(TournamentStatus.ACTIVE);
		return tournament;
	}

	private Match match(Tournament tournament) {
		Team home = new Team("Local");
		home.setId(10L);
		Team away = new Team("Visitante");
		away.setId(11L);
		MatchDay matchDay = new MatchDay("Fecha 1", tournament, 1);
		matchDay.setId(20L);
		Match match = new Match(tournament, matchDay, home, away, Instant.parse("2026-06-20T19:00:00Z"));
		match.setId(30L);
		return match;
	}

	private Prediction prediction(User user, Match match, Integer points, Boolean exactHit) {
		Prediction prediction = new Prediction(user, match);
		prediction.updatePrediction(1, 0, null);
		prediction.setPoints(points);
		prediction.setExactHit(exactHit);
		return prediction;
	}
}
