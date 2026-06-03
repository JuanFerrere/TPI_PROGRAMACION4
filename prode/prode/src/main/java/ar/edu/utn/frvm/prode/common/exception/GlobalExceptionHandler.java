package ar.edu.utn.frvm.prode.common.exception;

import ar.edu.utn.frvm.prode.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Manejador global de errores de la API.
 *
 * Centraliza las respuestas de error para que los controllers no mezclen logica
 * de negocio con armado manual de errores HTTP.
 */
@RestControllerAdvice // Captura excepciones lanzadas por controllers y devuelve respuestas JSON.
public class GlobalExceptionHandler {

	/**
	 * Maneja recursos duplicados como username o email repetidos.
	 *
	 * @param exception excepcion lanzada por el service.
	 * @param request peticion HTTP original, usada para conocer el path.
	 * @return respuesta HTTP 400 con cuerpo ErrorResponse.
	 */
	@ExceptionHandler(DuplicateResourceException.class) // Ejecuta este metodo ante DuplicateResourceException.
	public ResponseEntity<ErrorResponse> handleDuplicateResource(
			DuplicateResourceException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	/**
	 * Maneja reglas de negocio incumplidas.
	 *
	 * @param exception excepcion lanzada por el service.
	 * @param request peticion HTTP original, usada para conocer el path.
	 * @return respuesta HTTP 400 con cuerpo ErrorResponse.
	 */
	@ExceptionHandler(BusinessRuleException.class) // Ejecuta este metodo ante BusinessRuleException.
	public ResponseEntity<ErrorResponse> handleBusinessRule(
			BusinessRuleException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	/**
	 * Maneja recursos inexistentes.
	 *
	 * @param exception excepcion lanzada al no encontrar un recurso.
	 * @param request peticion HTTP original, usada para conocer el path.
	 * @return respuesta HTTP 404 con cuerpo ErrorResponse.
	 */
	@ExceptionHandler(ResourceNotFoundException.class) // Ejecuta este metodo ante ResourceNotFoundException.
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	/**
	 * Maneja credenciales invalidas durante el login.
	 *
	 * @param exception excepcion generada por Spring Security.
	 * @param request peticion HTTP original, usada para conocer el path.
	 * @return respuesta HTTP 401 con cuerpo ErrorResponse.
	 */
	@ExceptionHandler(BadCredentialsException.class) // Ejecuta este metodo cuando username/email o password no coinciden.
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", request);
	}

	/**
	 * Maneja errores de validacion producidos por @Valid.
	 *
	 * @param exception contiene todos los campos que fallaron validacion.
	 * @param request peticion HTTP original, usada para conocer el path.
	 * @return respuesta HTTP 400 con los mensajes de validacion.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class) // Ejecuta este metodo cuando falla una validacion de DTO.
	public ResponseEntity<ErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
	}

	/**
	 * Maneja cualquier error no contemplado explicitamente.
	 *
	 * @param exception error general de Java o Spring.
	 * @param request peticion HTTP original, usada para conocer el path.
	 * @return respuesta HTTP 500 con mensaje generico.
	 */
	@ExceptionHandler(Exception.class) // Ultima red de seguridad para errores no previstos.
	public ResponseEntity<ErrorResponse> handleGeneralError(
			Exception exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocurrio un error interno en el servidor",
				request
		);
	}

	/**
	 * Construye la respuesta de error con formato comun.
	 *
	 * @param status estado HTTP que corresponde al error.
	 * @param message mensaje que vera el cliente.
	 * @param request peticion HTTP original.
	 * @return ResponseEntity con status HTTP y cuerpo ErrorResponse.
	 */
	private ResponseEntity<ErrorResponse> buildErrorResponse(
			HttpStatus status,
			String message,
			HttpServletRequest request
	) {
		ErrorResponse response = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}
}
