package ar.edu.utn.frvm.prode.matchday.repository;

import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para fechas o jornadas.
 *
 * JpaRepository aporta operaciones CRUD y Spring Data genera consultas por nombre de metodo.
 */
public interface MatchDayRepository extends JpaRepository<MatchDay, Long> {

	/**
	 * Verifica si ya existe una fecha con ese nombre sin distinguir mayusculas/minusculas.
	 *
	 * @param name nombre a validar.
	 * @return true si el nombre ya existe, false si esta disponible.
	 */
	boolean existsByNameIgnoreCase(String name);

	/**
	 * Busca fechas por estado.
	 *
	 * @param status estado solicitado por el cliente.
	 * @return lista de fechas que tienen ese estado.
	 */
	List<MatchDay> findByStatus(MatchDayStatus status);
}
