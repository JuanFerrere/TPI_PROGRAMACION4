package ar.edu.utn.frvm.prode.auth.service;

import ar.edu.utn.frvm.prode.auth.dto.AuthResponse;
import ar.edu.utn.frvm.prode.auth.dto.LoginRequest;
import ar.edu.utn.frvm.prode.auth.dto.RegisterRequest;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.security.JwtService;
import ar.edu.utn.frvm.prode.user.dto.UserResponse;
import ar.edu.utn.frvm.prode.user.entity.Role;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private static final String TOKEN_TYPE = "Bearer";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		validateUsernameIsAvailable(request.username());
		validateEmailIsAvailable(request.email());

		String hashedPassword = passwordEncoder.encode(request.password());

		User user = new User(
				request.username(),
				request.email(),
				hashedPassword,
				Role.USER
		);

		User savedUser = userRepository.save(user);
		String token = jwtService.generateToken(savedUser);

		return toAuthResponse(savedUser, token);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.usernameOrEmail(),
							request.password()
					)
			);
		} catch (BadCredentialsException exception) {
			throw new BadCredentialsException("Credenciales invalidas");
		}

		User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
				.orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

		String token = jwtService.generateToken(user);

		return toAuthResponse(user, token);
	}

	@Transactional(readOnly = true)
	public UserResponse getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Usuario no autenticado");
		}

		String username = authentication.getName();

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));

		return toUserResponse(user);
	}

	private void validateUsernameIsAvailable(String username) {
		if (userRepository.existsByUsername(username)) {
			throw new DuplicateResourceException("El username ya esta registrado");
		}
	}

	private void validateEmailIsAvailable(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("El email ya esta registrado");
		}
	}

	private AuthResponse toAuthResponse(User user, String token) {
		return new AuthResponse(
				token,
				TOKEN_TYPE,
				user.getUsername(),
				user.getEmail(),
				user.getRole()
		);
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt()
		);
	}
}
