package ar.edu.utn.frvm.prode.config;

import ar.edu.utn.frvm.prode.security.JwtAuthenticationEntryPoint;
import ar.edu.utn.frvm.prode.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion principal de seguridad para la API.
 *
 * Esta clase reemplaza la configuracion temporal de Etapa 1 y prepara seguridad stateless con JWT.
 */
@Configuration // Indica que esta clase declara beans de configuracion para Spring.
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	/**
	 * Constructor con dependencias de seguridad.
	 *
	 * @param jwtAuthenticationFilter filtro que lee y valida JWT.
	 * @param jwtAuthenticationEntryPoint componente que responde 401 ante falta de autenticacion.
	 */
	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
	}

	/**
	 * Define las reglas de seguridad HTTP de la API.
	 *
	 * @param http objeto de Spring Security usado para configurar filtros y permisos.
	 * @return SecurityFilterChain con la configuracion final.
	 */
	@Bean // Registra la cadena de filtros de seguridad como bean de Spring.
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable) // Desactiva CSRF porque esta API usa JWT y no sesiones web con cookies.
				.formLogin(AbstractHttpConfigurer::disable) // Desactiva el formulario de login HTML propio de Spring Security.
				.httpBasic(AbstractHttpConfigurer::disable) // Desactiva autenticacion Basic para usar JWT por header Authorization.
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No guarda sesion en servidor: cada request debe traer token.
				)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint) // Devuelve 401 JSON cuando falta autenticacion.
				)
				.authorizeHttpRequests(authorization -> authorization
						.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll() // Registro publico.
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() // Login publico.
						.requestMatchers(HttpMethod.GET, "/api/health").permitAll() // Health check publico para pruebas.
						.anyRequest().authenticated() // Todo lo demas requiere JWT valido.
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Ejecuta nuestro filtro JWT antes del login tradicional.
				.build(); // Construye la configuracion final.
	}

	/**
	 * Define el algoritmo para hashear contrasenas.
	 *
	 * BCrypt agrega sal y costo computacional para no guardar contrasenas en texto plano.
	 *
	 * @return PasswordEncoder basado en BCrypt.
	 */
	@Bean // Permite inyectar PasswordEncoder en services y seeders.
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Expone el AuthenticationManager de Spring Security.
	 *
	 * AuthenticationManager valida credenciales usando UserDetailsService y PasswordEncoder.
	 *
	 * @param authenticationConfiguration configuracion interna de autenticacion.
	 * @return AuthenticationManager listo para usar en AuthService.
	 */
	@Bean // Permite inyectar AuthenticationManager en AuthService.
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration
	) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
