package ar.edu.utn.frvm.prode.user.repository;

import ar.edu.utn.frvm.prode.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de usuarios.
 *
 * Un repositorio es la capa encargada de consultar y guardar datos en la base.
 * JpaRepository ya trae metodos como save, findById, findAll y deleteById.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Busca un usuario por nombre de usuario.
	 *
	 * @param username nombre de usuario a buscar.
	 * @return Optional con el usuario si existe, o Optional vacio si no existe.
	 */
	Optional<User> findByUsername(String username);

	/**
	 * Busca un usuario por email.
	 *
	 * @param email email a buscar.
	 * @return Optional con el usuario si existe, o Optional vacio si no existe.
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Busca un usuario por username o por email.
	 *
	 * Optional evita devolver null. Obliga a manejar explicitamente el caso "no encontrado".
	 *
	 * @param username nombre de usuario a buscar.
	 * @param email email a buscar.
	 * @return Optional con el usuario si existe, o Optional vacio si no existe.
	 */
	Optional<User> findByUsernameOrEmail(String username, String email);

	/**
	 * Verifica si ya existe un usuario con ese username.
	 *
	 * @param username nombre de usuario a verificar.
	 * @return true si existe, false si no existe.
	 */
	boolean existsByUsername(String username);

	/**
	 * Verifica si ya existe un usuario con ese email.
	 *
	 * @param email email a verificar.
	 * @return true si existe, false si no existe.
	 */
	boolean existsByEmail(String email);
}
