package ar.edu.utn.frvm.prode.group.repository;

import ar.edu.utn.frvm.prode.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para membresías de grupos.
 *
 * Spring Data JPA genera las consultas automáticamente. Los métodos navegan
 * las relaciones: GroupMember.group.id y GroupMember.user.id.
 *
 * El método deleteByGroupIdAndUserId ejecuta un find + delete dentro de la
 * transacción del service que lo invoca.
 */
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

	/**
	 * Verifica si un usuario ya pertenece a un grupo.
	 *
	 * Previene membresías duplicadas al unirse con el mismo código.
	 *
	 * @param groupId id del grupo.
	 * @param userId  id del usuario.
	 * @return true si el usuario ya es miembro del grupo.
	 */
	boolean existsByGroupIdAndUserId(Long groupId, Long userId);

	/**
	 * Busca la membresía específica de un usuario en un grupo.
	 *
	 * Se usa al salir de un grupo para obtener el registro a eliminar.
	 *
	 * @param groupId id del grupo.
	 * @param userId  id del usuario.
	 * @return Optional con la membresía si existe.
	 */
	Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

	/**
	 * Lista todas las membresías de un usuario.
	 *
	 * Se usa para listar los grupos a los que pertenece el usuario autenticado.
	 *
	 * @param userId id del usuario.
	 * @return lista de membresías del usuario.
	 */
	List<GroupMember> findByUserId(Long userId);

	/**
	 * Lista todos los miembros de un grupo.
	 *
	 * Se usa para obtener el detalle del grupo y para calcular el ranking.
	 *
	 * @param groupId id del grupo.
	 * @return lista de membresías del grupo.
	 */
	List<GroupMember> findByGroupId(Long groupId);

	/**
	 * Cuenta cuántos miembros tiene un grupo.
	 *
	 * Se usa para incluir el conteo en la respuesta sin cargar todos los miembros.
	 *
	 * @param groupId id del grupo.
	 * @return cantidad de miembros.
	 */
	long countByGroupId(Long groupId);

	/**
	 * Elimina la membresía de un usuario en un grupo.
	 *
	 * Debe ejecutarse dentro de una transacción activa (que provee el service).
	 *
	 * @param groupId id del grupo.
	 * @param userId  id del usuario a remover.
	 */
	void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
