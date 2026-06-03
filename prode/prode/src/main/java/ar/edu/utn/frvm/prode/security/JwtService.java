package ar.edu.utn.frvm.prode.security;

import ar.edu.utn.frvm.prode.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Servicio encargado de trabajar con JWT.
 *
 * JWT significa JSON Web Token. Es un texto firmado que permite identificar
 * al usuario sin guardar sesion en el servidor.
 */
@Service // Registra esta clase como service de Spring para poder inyectarla en otros componentes.
public class JwtService {

	private final String jwtSecret;
	private final long jwtExpirationMs;

	/**
	 * Constructor que recibe valores configurados en application.properties.
	 *
	 * @param jwtSecret clave secreta usada para firmar tokens.
	 * @param jwtExpirationMs duracion del token en milisegundos.
	 */
	public JwtService(
			@Value("${app.jwt.secret}") String jwtSecret, // Lee app.jwt.secret desde application.properties.
			@Value("${app.jwt.expiration-ms}") long jwtExpirationMs // Lee app.jwt.expiration-ms desde application.properties.
	) {
		this.jwtSecret = jwtSecret;
		this.jwtExpirationMs = jwtExpirationMs;
	}

	/**
	 * Genera un JWT para un usuario autenticado.
	 *
	 * @param user usuario para el que se genera el token.
	 * @return token JWT firmado.
	 */
	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(jwtExpirationMs);

		return Jwts.builder()
				.subject(user.getUsername()) // El subject identifica al usuario principal del token.
				.claim("role", user.getRole().name()) // Guarda el rol como dato adicional del token.
				.issuedAt(Date.from(now)) // Fecha de emision del token.
				.expiration(Date.from(expiration)) // Fecha en la que el token deja de ser valido.
				.signWith(getSigningKey()) // Firma el token con la clave secreta.
				.compact(); // Convierte el token a String.
	}

	/**
	 * Extrae el username guardado dentro del token.
	 *
	 * @param token JWT recibido desde el cliente.
	 * @return username guardado como subject del token.
	 */
	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	/**
	 * Verifica si el token pertenece al usuario esperado y si no esta vencido.
	 *
	 * @param token JWT recibido desde el cliente.
	 * @param userDetails usuario cargado desde la base de datos.
	 * @return true si el token es valido, false si no lo es.
	 */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	/**
	 * Verifica si el token ya expiro.
	 *
	 * @param token JWT recibido desde el cliente.
	 * @return true si esta vencido, false si sigue vigente.
	 */
	private boolean isTokenExpired(String token) {
		Date expiration = extractAllClaims(token).getExpiration();
		return expiration.before(new Date());
	}

	/**
	 * Extrae todos los datos internos del JWT.
	 *
	 * Claims son los datos guardados dentro del token, como subject, role y expiration.
	 *
	 * @param token JWT recibido desde el cliente.
	 * @return claims firmados y validados.
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey()) // Verifica que la firma del token coincida con nuestra clave.
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/**
	 * Construye la clave criptografica usada para firmar y validar tokens.
	 *
	 * @return clave secreta compatible con HMAC.
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
