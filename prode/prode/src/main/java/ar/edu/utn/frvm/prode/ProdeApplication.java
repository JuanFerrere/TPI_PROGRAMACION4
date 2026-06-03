package ar.edu.utn.frvm.prode;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicacion Spring Boot.
 *
 * Desde aca se inicia el backend y, antes de arrancar Spring, se cargan variables
 * desde un archivo .env local si existe.
 */
@SpringBootApplication
public class ProdeApplication {

	/**
	 * Punto de entrada de la aplicacion.
	 *
	 * @param args argumentos opcionales recibidos al ejecutar la aplicacion.
	 */
	public static void main(String[] args) {
		loadEnvironmentVariables();
		SpringApplication.run(ProdeApplication.class, args);
	}

	/**
	 * Intenta cargar variables desde un archivo .env local.
	 *
	 * Dotenv permite leer un archivo .env durante desarrollo.
	 * ignoreIfMissing evita que la aplicacion falle automaticamente si el archivo no existe.
	 * Esto es util porque en produccion las variables suelen venir del sistema operativo o del servidor.
	 */
	private static void loadEnvironmentVariables() {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // Si no existe .env, no se corta el arranque: Spring podra usar variables del sistema.
				.load(); // Lee el archivo .env si esta presente en la raiz del proyecto.

		setSystemPropertyIfPresent(dotenv, "DB_URL"); // URL de conexion a PostgreSQL.
		setSystemPropertyIfPresent(dotenv, "DB_USERNAME"); // Usuario de PostgreSQL.
		setSystemPropertyIfPresent(dotenv, "DB_PASSWORD"); // Password real de PostgreSQL.
		setSystemPropertyIfPresent(dotenv, "JWT_SECRET"); // Clave secreta para firmar JWT.
		setSystemPropertyIfPresent(dotenv, "JWT_EXPIRATION_MS"); // Duracion del JWT en milisegundos.
		setSystemPropertyIfPresent(dotenv, "APP_ADMIN_USERNAME"); // Username del admin inicial.
		setSystemPropertyIfPresent(dotenv, "APP_ADMIN_EMAIL"); // Email del admin inicial.
		setSystemPropertyIfPresent(dotenv, "APP_ADMIN_PASSWORD"); // Password del admin inicial.
	}

	/**
	 * Carga una variable del .env como System property si corresponde.
	 *
	 * @param dotenv archivo .env ya cargado por dotenv-java.
	 * @param key nombre de la variable que se quiere cargar.
	 */
	private static void setSystemPropertyIfPresent(Dotenv dotenv, String key) {
		// Si ya existe una System property, no se pisa porque pudo venir desde IntelliJ o desde argumentos JVM.
		if (System.getProperty(key) != null && !System.getProperty(key).isBlank()) {
			return;
		}

		// Si ya existe una variable de entorno real del sistema operativo, Spring Boot puede leerla directamente.
		if (System.getenv(key) != null && !System.getenv(key).isBlank()) {
			return;
		}

		// Busca el valor dentro del archivo .env cargado por dotenv-java.
		String value = dotenv.get(key);

		// No se cargan valores nulos o vacios para evitar placeholders mal configurados.
		if (value == null || value.isBlank()) {
			return;
		}

		// Se carga como System property para que Spring pueda resolver expresiones como ${DB_PASSWORD}.
		System.setProperty(key, value);
	}
}
