package ar.edu.utn.frvm.prode.seed;

import ar.edu.utn.frvm.prode.user.entity.Role;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos iniciales para desarrollo.
 *
 * En esta etapa crea un usuario ADMIN para poder probar permisos futuros.
 */
@Component // Spring ejecuta este componente al iniciar la aplicacion.
public class DataSeeder implements CommandLineRunner {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_EMAIL = "admin@prode.local";
	private static final String ADMIN_PASSWORD = "Admin1234!";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Constructor con dependencias necesarias para crear el admin.
	 *
	 * @param userRepository repositorio para consultar y guardar usuarios.
	 * @param passwordEncoder componente que hashea la password con BCrypt.
	 */
	public DataSeeder(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Metodo ejecutado automaticamente al iniciar Spring Boot.
	 *
	 * @param args argumentos de inicio de la aplicacion.
	 */
	@Override // Implementa el metodo requerido por CommandLineRunner.
	public void run(String... args) {
		if (userRepository.existsByUsername(ADMIN_USERNAME) || userRepository.existsByEmail(ADMIN_EMAIL)) {
			System.out.println("Admin inicial ya existe. No se crea duplicado.");
			return;
		}

		User admin = new User(
				ADMIN_USERNAME,
				ADMIN_EMAIL,
				passwordEncoder.encode(ADMIN_PASSWORD),
				Role.ADMIN
		);

		userRepository.save(admin);
		System.out.println("Admin inicial creado: username=admin, email=admin@prode.local");
	}
}
