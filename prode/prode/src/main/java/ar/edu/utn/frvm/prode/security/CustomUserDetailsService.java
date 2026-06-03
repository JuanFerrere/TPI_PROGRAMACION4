package ar.edu.utn.frvm.prode.security;

import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que adapta nuestros usuarios al formato que entiende Spring Security.
 *
 * UserDetails es una interfaz de Spring Security que representa al usuario autenticado.
 */
@Service // Spring registra esta clase para usarla durante login y validacion de tokens.
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	/**
	 * Constructor con inyeccion de dependencias.
	 *
	 * @param userRepository repositorio usado para buscar usuarios en PostgreSQL.
	 */
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Carga un usuario por username o email.
	 *
	 * Spring Security llama a este metodo durante la autenticacion.
	 *
	 * @param usernameOrEmail username o email ingresado por el usuario.
	 * @return UserDetails con username, password hasheada y autoridades.
	 */
	@Override // Indica que estamos implementando el metodo definido por UserDetailsService.
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getUsername()) // Spring usara este username como identificador autenticado.
				.password(user.getPassword()) // Password hasheada con BCrypt, no contrasena real.
				.authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))) // Convierte Role en autoridad.
				.build();
	}
}
