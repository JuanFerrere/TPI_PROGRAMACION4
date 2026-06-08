package ar.edu.utn.frvm.prode.match.repository;

import ar.edu.utn.frvm.prode.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para partidos.
 *
 * Centraliza consultas a la tabla matches y evita escribir SQL manual para casos simples.
 */
public interface MatchRepository extends JpaRepository<Match, Long> {

	/**
	 * Verifica si una fecha tiene partidos asociados.
	 *
	 * @param matchDayId id de la fecha.
	 * @return true si existe al menos un partido de esa fecha.
	 */
	boolean existsByMatchDayId(Long matchDayId);

	/**
	 * Verifica si un equipo esta usado como local en algun partido.
	 *
	 * @param teamId id del equipo.
	 * @return true si el equipo esta asociado como local.
	 */
	boolean existsByHomeTeamId(Long teamId);

	/**
	 * Verifica si un equipo esta usado como visitante en algun partido.
	 *
	 * @param teamId id del equipo.
	 * @return true si el equipo esta asociado como visitante.
	 */
	boolean existsByAwayTeamId(Long teamId);

	/**
	 * Lista partidos de una fecha ordenados por horario de inicio ascendente.
	 *
	 * @param matchDayId id de la fecha.
	 * @return lista ordenada de partidos.
	 */
	List<Match> findByMatchDayIdOrderByStartTimeAsc(Long matchDayId);

	/**
	 * Lista todos los partidos ordenados por horario de inicio ascendente.
	 *
	 * @return lista completa ordenada por startTime asc.
	 */
	List<Match> findAllByOrderByStartTimeAsc();
}
