package ar.edu.utn.frvm.prode.tournament.repository;

import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Repositorio JPA para torneos.
 */
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

	/**
	 * Verifica existencia por nombre ignorando mayusculas y minusculas.
	 *
	 * @param name nombre normalizado del torneo.
	 * @return true si ya existe un torneo con ese nombre.
	 */
	boolean existsByNameIgnoreCase(String name);

	/**
	 * Lista torneos desde el mas reciente al mas antiguo.
	 *
	 * @return torneos ordenados por fecha de creacion descendente.
	 */
	List<Tournament> findAllByOrderByCreatedAtDesc();

	List<Tournament> findByStatusInOrderByCreatedAtDesc(Collection<TournamentStatus> statuses);
}
