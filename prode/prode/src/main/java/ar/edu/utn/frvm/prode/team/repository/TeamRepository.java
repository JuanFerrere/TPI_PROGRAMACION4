package ar.edu.utn.frvm.prode.team.repository;

import ar.edu.utn.frvm.prode.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para acceder a equipos en la base de datos.
 *
 * Spring Data genera automaticamente las consultas a partir del nombre de los metodos.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

	/**
	 * Verifica si existe un equipo con el mismo nombre, sin distinguir mayusculas/minusculas.
	 *
	 * @param name nombre a validar.
	 * @return true si ya existe, false si esta disponible.
	 */
	boolean existsByNameIgnoreCase(String name);

	/**
	 * Busca un equipo por nombre exacto ignorando mayusculas/minusculas.
	 *
	 * @param name nombre a buscar.
	 * @return Optional con el equipo si existe, o vacio si no existe.
	 */
	Optional<Team> findByNameIgnoreCase(String name);

	/**
	 * Busca equipos cuyo nombre contenga el texto recibido.
	 *
	 * @param name texto parcial a buscar.
	 * @return lista de equipos ordenada alfabeticamente por nombre.
	 */
	List<Team> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
