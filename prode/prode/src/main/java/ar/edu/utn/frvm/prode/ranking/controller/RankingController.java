package ar.edu.utn.frvm.prode.ranking.controller;

import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.ranking.service.RankingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para el ranking.
 *
 * Expone la tabla de posiciones bajo /api/rankings y delega el calculo en RankingService.
 */
@RestController // Indica que la clase recibe HTTP y responde JSON.
@RequestMapping("/api/rankings") // Prefijo comun para endpoints de ranking.
public class RankingController {

	private final RankingService rankingService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param rankingService service que calcula el ranking.
	 */
	public RankingController(RankingService rankingService) {
		this.rankingService = rankingService;
	}

	/**
	 * Devuelve el ranking global ordenado por puntos.
	 *
	 * Rol permitido: cualquier usuario autenticado. Si todavia no hay pronosticos
	 * con puntos, la lista llega vacia (no es un error).
	 *
	 * @return lista ordenada de posiciones del ranking global.
	 */
	@GetMapping("/global") // Atiende GET /api/rankings/global.
	@PreAuthorize("isAuthenticated()") // Requiere JWT valido, sin exigir ADMIN.
	public List<RankingResponse> getGlobalRanking() {
		return rankingService.getGlobalRanking();
	}
}
