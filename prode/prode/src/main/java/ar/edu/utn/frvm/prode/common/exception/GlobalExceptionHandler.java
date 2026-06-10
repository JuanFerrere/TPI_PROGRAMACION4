package ar.edu.utn.frvm.prode.common.exception;

import ar.edu.utn.frvm.prode.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateResource(
			DuplicateResourceException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ErrorResponse> handleBusinessRule(
			BusinessRuleException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(
			AccessDeniedException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(HttpStatus.FORBIDDEN, "No tenes permisos para acceder a este recurso", request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
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

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		String message = "El parametro '" + exception.getName() + "' tiene un valor invalido";
		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableJson(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		return buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"El body JSON no tiene un formato valido. Verificar tipos de datos y fechas ISO-8601 UTC",
				request
		);
	}

	@ExceptionHandler(Exception.class)
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
