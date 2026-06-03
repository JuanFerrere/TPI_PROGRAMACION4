package ar.edu.utn.frvm.prode.auth.controller;

import ar.edu.utn.frvm.prode.auth.dto.AuthResponse;
import ar.edu.utn.frvm.prode.auth.dto.LoginRequest;
import ar.edu.utn.frvm.prode.auth.dto.RegisterRequest;
import ar.edu.utn.frvm.prode.auth.service.AuthService;
import ar.edu.utn.frvm.prode.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST de autenticacion.
 *
 * Expone endpoints para registro, login y consulta del usuario autenticado.
 */
@RestController // Indica que esta clase recibe HTTP y responde JSON.
@RequestMapping("/api/auth") // Define el prefijo comun para todos los endpoints de autenticacion.
public class AuthController {

	private final AuthService authService;

	/**
	 * Constructor con inyeccion del service.
	 *
	 * @param authService servicio que contiene la logica de autenticacion.
	 */
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Registra un usuario nuevo.
	 *
	 * @param request cuerpo JSON con username, email y password.
	 * @return AuthResponse con JWT y datos publicos del usuario.
	 */
	@PostMapping("/register") // Atiende POST /api/auth/register.
	@ResponseStatus(HttpStatus.CREATED) // Devuelve HTTP 201 cuando el registro fue exitoso.
	public AuthResponse register(
			@Valid // Ejecuta validaciones como @NotBlank, @Email y @Size antes de entrar al metodo.
			@RequestBody RegisterRequest request // Convierte el JSON recibido en un DTO Java.
	) {
		return authService.register(request);
	}

	/**
	 * Inicia sesion con username o email.
	 *
	 * @param request cuerpo JSON con usernameOrEmail y password.
	 * @return AuthResponse con JWT y datos publicos del usuario.
	 */
	@PostMapping("/login") // Atiende POST /api/auth/login.
	public AuthResponse login(
			@Valid // Valida que los campos obligatorios esten presentes.
			@RequestBody LoginRequest request // Convierte el JSON recibido en LoginRequest.
	) {
		return authService.login(request);
	}

	/**
	 * Devuelve el usuario autenticado segun el JWT recibido.
	 *
	 * @return UserResponse con datos publicos del usuario autenticado.
	 */
	@GetMapping("/me") // Atiende GET /api/auth/me. Esta ruta requiere token por SecurityConfig.
	public UserResponse me() {
		return authService.getCurrentUser();
	}
}
