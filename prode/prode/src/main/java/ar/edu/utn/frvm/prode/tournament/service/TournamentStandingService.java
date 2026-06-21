package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.tournament.dto.StandingRowResponse;
import ar.edu.utn.frvm.prode.tournament.dto.StandingsGroupResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStandingsResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentFormat;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service que arma la tabla deportiva de equipos de un torneo.
 *
 * La tabla no se persiste: se calcula en memoria a partir de los partidos
 * FINALIZADO del torneo. Para el tamano de este TPI es claro y suficiente.
 */
@Service
public class TournamentStandingService {

	private static final int POINTS_PER_WIN = 3;
	private static final int POINTS_PER_DRAW = 1;

	private final TournamentRepository tournamentRepository;
	private final TournamentTeamRepository tournamentTeamRepository;
	private final MatchRepository matchRepository;

	public TournamentStandingService(
			TournamentRepository tournamentRepository,
			TournamentTeamRepository tournamentTeamRepository,
			MatchRepository matchRepository
	) {
		this.tournamentRepository = tournamentRepository;
		this.tournamentTeamRepository = tournamentTeamRepository;
		this.matchRepository = matchRepository;
	}

	/**
	 * Calcula la tabla deportiva del torneo.
	 *
	 * Pasos:
	 * 1. Inicializa la tabla con todos los equipos del torneo (los que no jugaron quedan en cero).
	 * 2. Acumula los resultados de los partidos finalizados.
	 * 3. Agrupa (por grupo si es GROUPS, una sola tabla si es LEAGUE), ordena y asigna posiciones.
	 *
	 * @param tournamentId id del torneo.
	 * @return tabla deportiva agrupada y ordenada.
	 */
	@Transactional(readOnly = true)
	public TournamentStandingsResponse getStandings(Long tournamentId) {
		Tournament tournament = tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));

		boolean byGroups = tournament.getFormat() == TournamentFormat.GROUPS;

		Map<Long, TeamStanding> standingByTeam = new LinkedHashMap<>();
		for (TournamentTeam tournamentTeam : tournamentTeamRepository.findByTournamentIdOrdered(tournamentId)) {
			String groupName = byGroups ? tournamentTeam.getGroupName() : null;
			standingByTeam.put(
					tournamentTeam.getTeam().getId(),
					new TeamStanding(
							tournamentTeam.getTeam().getId(),
							tournamentTeam.getTeam().getName(),
							groupName
					)
			);
		}

		List<Match> finishedMatches = matchRepository
				.findByTournamentIdAndStatusOrderByStartTimeAsc(tournamentId, MatchStatus.FINALIZADO);
		for (Match match : finishedMatches) {
			if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
				continue;
			}
			TeamStanding home = standingByTeam.get(match.getHomeTeam().getId());
			TeamStanding away = standingByTeam.get(match.getAwayTeam().getId());
			if (home == null || away == null) {
				continue;
			}
			home.addResult(match.getHomeGoals(), match.getAwayGoals());
			away.addResult(match.getAwayGoals(), match.getHomeGoals());
		}

		return buildResponse(tournament, standingByTeam.values());
	}

	private TournamentStandingsResponse buildResponse(Tournament tournament, Collection<TeamStanding> standings) {
		// Agrupa manteniendo el orden de aparicion (la query trae los equipos ordenados por grupo).
		Map<String, List<TeamStanding>> standingsByGroup = new LinkedHashMap<>();
		for (TeamStanding standing : standings) {
			standingsByGroup
					.computeIfAbsent(standing.getGroupName(), key -> new ArrayList<>())
					.add(standing);
		}

		// Reglas de desempate, en orden de prioridad: puntos, diferencia de gol, goles a favor, nombre.
		Comparator<TeamStanding> standingOrder = Comparator
				.comparingInt(TeamStanding::getPoints).reversed()
				.thenComparing(Comparator.comparingInt(TeamStanding::getGoalDifference).reversed())
				.thenComparing(Comparator.comparingInt(TeamStanding::getGoalsFor).reversed())
				.thenComparing(standing -> standing.getTeamName().toLowerCase());

		List<StandingsGroupResponse> groups = new ArrayList<>();
		for (Map.Entry<String, List<TeamStanding>> entry : standingsByGroup.entrySet()) {
			List<TeamStanding> ordered = entry.getValue().stream()
					.sorted(standingOrder)
					.toList();

			List<StandingRowResponse> rows = new ArrayList<>();
			int position = 1;
			for (TeamStanding standing : ordered) {
				rows.add(new StandingRowResponse(
						position,
						standing.getTeamId(),
						standing.getTeamName(),
						standing.getGroupName(),
						standing.getPlayed(),
						standing.getWon(),
						standing.getDrawn(),
						standing.getLost(),
						standing.getGoalsFor(),
						standing.getGoalsAgainst(),
						standing.getGoalDifference(),
						standing.getPoints()
				));
				position++;
			}

			groups.add(new StandingsGroupResponse(entry.getKey(), rows));
		}

		return new TournamentStandingsResponse(
				tournament.getId(),
				tournament.getName(),
				tournament.getFormat(),
				groups
		);
	}

	/**
	 * Acumulador interno con las estadisticas de un equipo mientras se recorren los partidos.
	 */
	private static final class TeamStanding {
		private final Long teamId;
		private final String teamName;
		private final String groupName;
		private int played;
		private int won;
		private int drawn;
		private int lost;
		private int goalsFor;
		private int goalsAgainst;

		private TeamStanding(Long teamId, String teamName, String groupName) {
			this.teamId = teamId;
			this.teamName = teamName;
			this.groupName = groupName;
		}

		private void addResult(int goalsScored, int goalsConceded) {
			this.played++;
			this.goalsFor += goalsScored;
			this.goalsAgainst += goalsConceded;
			if (goalsScored > goalsConceded) {
				this.won++;
			} else if (goalsScored == goalsConceded) {
				this.drawn++;
			} else {
				this.lost++;
			}
		}

		private Long getTeamId() {
			return teamId;
		}

		private String getTeamName() {
			return teamName;
		}

		private String getGroupName() {
			return groupName;
		}

		private int getPlayed() {
			return played;
		}

		private int getWon() {
			return won;
		}

		private int getDrawn() {
			return drawn;
		}

		private int getLost() {
			return lost;
		}

		private int getGoalsFor() {
			return goalsFor;
		}

		private int getGoalsAgainst() {
			return goalsAgainst;
		}

		private int getGoalDifference() {
			return goalsFor - goalsAgainst;
		}

		private int getPoints() {
			return won * POINTS_PER_WIN + drawn * POINTS_PER_DRAW;
		}
	}
}
