package ar.edu.utn.frvm.prode.group.repository;

import ar.edu.utn.frvm.prode.group.entity.PrivateGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para grupos privados.
 *
 * Spring Data JPA genera las consultas automáticamente a partir del nombre de
 * los métodos. Los métodos de búsqueda por código de invitación son los más
 * usados: al unirse a un grupo el código es el único dato que el cliente envía.
 */
public interface PrivateGroupRepository extends JpaRepository<PrivateGroup, Long> {

	/**
	 * Busca un grupo por su código de invitación.
	 *
	 * Se usa al procesar la solicitud de unirse a un grupo.
	 *
	 * @param inviteCode código de invitación enviado por el cliente.
	 * @return Optional con el grupo si existe.
	 */
	Optional<PrivateGroup> findByInviteCode(String inviteCode);

	/**
	 * Verifica si ya existe un grupo con el código de invitación dado.
	 *
	 * Se usa en la generación del código para garantizar unicidad antes de guardar.
	 *
	 * @param inviteCode código a verificar.
	 * @return true si ya existe un grupo con ese código.
	 */
	boolean existsByInviteCode(String inviteCode);
}
