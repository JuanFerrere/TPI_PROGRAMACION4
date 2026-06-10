package ar.edu.utn.frvm.prode.ranking.dto;

public record RankingResponse(
		Integer position,
		Long userId,
		String username,
		Integer totalPoints,
		Long exactHits,
		Long predictionsCount
) {
}
