package ar.edu.utn.frvm.prode.seed;

import ar.edu.utn.frvm.prode.user.entity.Role;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String adminUsername;
	private final String adminEmail;
	private final String adminPassword;

	/**
	 * Constructor con dependencias necesarias para crear el admin.
	 *
	 * @param userRepository repositorio para consultar y guardar usuarios.
	 * @param passwordEncoder componente que hashea la password con BCrypt.
	 * @param adminUsername username del admin leido desde app.admin.username.
	 * @param adminEmail email del admin leido desde app.admin.email.
	 * @param adminPassword password del admin leida desde app.admin.password.
	 */
	public DataSeeder(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin.username}") String adminUsername,
			@Value("${app.admin.email}") String adminEmail,
			@Value("${app.admin.password}") String adminPassword
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminUsername = adminUsername;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	/**
	 * Metodo ejecutado automaticamente al iniciar Spring Boot.
	 *
	 * @param args argumentos de inicio de la aplicacion.
	 */
	@Override // Implementa el metodo requerido por CommandLineRunner.
	public void run(String... args) {
		if (adminPassword == null || adminPassword.isBlank()) {
			System.out.println("No se creo el admin inicial: falta configurar APP_ADMIN_PASSWORD.");
			return;
		}

		if (userRepository.existsByUsername(adminUsername) || userRepository.existsByEmail(adminEmail)) {
			System.out.println("Admin inicial ya existe. No se crea duplicado.");
			return;
		}

		User admin = new User(
				adminUsername,
				adminEmail,
				passwordEncoder.encode(adminPassword),
				Role.ADMIN
		);

		userRepository.save(admin);
		System.out.println("Admin inicial creado: username=" + adminUsername + ", email=" + adminEmail);
	}
}
