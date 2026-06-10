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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(
			@Valid
			@RequestBody RegisterRequest request
	) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(
			@Valid
			@RequestBody LoginRequest request
	) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public UserResponse me() {
		return authService.getCurrentUser();
	}
}
