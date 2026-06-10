package ar.edu.utn.frvm.prode;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProdeApplication {
	public static void main(String[] args) {
		loadEnvironmentVariables();
		SpringApplication.run(ProdeApplication.class, args);
	}

	private static void loadEnvironmentVariables() {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		setSystemPropertyIfPresent(dotenv, "DB_URL");
		setSystemPropertyIfPresent(dotenv, "DB_USERNAME");
		setSystemPropertyIfPresent(dotenv, "DB_PASSWORD");
		setSystemPropertyIfPresent(dotenv, "JWT_SECRET");
		setSystemPropertyIfPresent(dotenv, "JWT_EXPIRATION_MS");
		setSystemPropertyIfPresent(dotenv, "APP_ADMIN_USERNAME");
		setSystemPropertyIfPresent(dotenv, "APP_ADMIN_EMAIL");
		setSystemPropertyIfPresent(dotenv, "APP_ADMIN_PASSWORD");
	}

	private static void setSystemPropertyIfPresent(Dotenv dotenv, String key) {
		if (System.getProperty(key) != null && !System.getProperty(key).isBlank()) {
			return;
		}

		if (System.getenv(key) != null && !System.getenv(key).isBlank()) {
			return;
		}

		String value = dotenv.get(key);

		if (value == null || value.isBlank()) {
			return;
		}

		System.setProperty(key, value);
	}
}
