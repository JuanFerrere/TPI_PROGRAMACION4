package ar.edu.utn.frvm.prode.config;

import ar.edu.utn.frvm.prode.common.dto.ErrorResponse;
import ar.edu.utn.frvm.prode.security.JwtAuthenticationEntryPoint;
import ar.edu.utn.frvm.prode.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final ObjectMapper objectMapper;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
			ObjectMapper objectMapper
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.objectMapper = objectMapper;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(jwtAuthenticationEntryPoint)
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							ErrorResponse errorResponse = new ErrorResponse(
									Instant.now(),
									HttpStatus.FORBIDDEN.value(),
									HttpStatus.FORBIDDEN.getReasonPhrase(),
									"No tenes permisos para acceder a este recurso",
									request.getRequestURI()
							);

							response.setStatus(HttpStatus.FORBIDDEN.value());
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							objectMapper.writeValue(response.getWriter(), errorResponse);
						})
				)
				.authorizeHttpRequests(authorization -> authorization
						.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/health").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration
	) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
