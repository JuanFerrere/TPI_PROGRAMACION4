package ar.edu.utn.frvm.prode.tournament.controller;

import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.ranking.service.RankingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para rankings de usuarios filtrados por torneo.
 */
@RestController
@RequestMapping("/api/tournaments/{tournamentId}/rankings")
public class TournamentRankingController {

	private final RankingService rankingService;

	public TournamentRankingController(RankingService rankingService) {
		this.rankingService = rankingService;
	}

	@GetMapping("/global")
	@PreAuthorize("isAuthenticated()")
	public List<RankingResponse> getTournamentGlobalRanking(
			@PathVariable Long tournamentId
	) {
		return rankingService.getTournamentGlobalRanking(tournamentId);
	}
}
