package ar.edu.utn.frvm.prode.ranking.controller;

import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.ranking.service.RankingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {
	private final RankingService rankingService;

	public RankingController(RankingService rankingService) {
		this.rankingService = rankingService;
	}

	@GetMapping("/global")
	@PreAuthorize("isAuthenticated()")
	public List<RankingResponse> getGlobalRanking() {
		return rankingService.getGlobalRanking();
	}
}
