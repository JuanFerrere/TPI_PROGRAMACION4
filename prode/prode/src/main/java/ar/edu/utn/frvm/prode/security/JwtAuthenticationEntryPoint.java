package ar.edu.utn.frvm.prode.security;

import ar.edu.utn.frvm.prode.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Componente que define la respuesta cuando un endpoint protegido se intenta usar sin autenticacion valida.
 *
 * AuthenticationEntryPoint es el punto de entrada de Spring Security para responder errores 401.
 */
@Component // Spring registra esta clase para usarla dentro de SecurityConfig.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	/**
	 * Constructor con ObjectMapper.
	 *
	 * @param objectMapper herramienta de Jackson para convertir objetos Java a JSON.
	 */
	public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Construye la respuesta 401 cuando no hay token o el token no es valido.
	 *
	 * @param request peticion HTTP original.
	 * @param response respuesta HTTP que se enviara al cliente.
	 * @param authException excepcion de autenticacion detectada por Spring Security.
	 */
	@Override // Implementa el metodo exigido por AuthenticationEntryPoint.
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException, ServletException {
		ErrorResponse errorResponse = new ErrorResponse(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				"Se requiere un token valido para acceder a este recurso",
				request.getRequestURI()
		);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), errorResponse);
	}
}
