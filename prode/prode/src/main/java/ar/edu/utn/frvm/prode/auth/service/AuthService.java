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

/**
 * Servicio de autenticacion.
 *
 * Contiene la logica de registro, login y consulta del usuario autenticado.
 */
@Service // Marca esta clase como componente de logica de negocio administrado por Spring.
public class AuthService {

	private static final String TOKEN_TYPE = "Bearer";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	/**
	 * Constructor con todas las dependencias necesarias.
	 *
	 * @param userRepository repositorio para consultar y guardar usuarios.
	 * @param passwordEncoder componente que hashea y verifica contrasenas con BCrypt.
	 * @param jwtService servicio que genera y valida tokens JWT.
	 * @param authenticationManager componente de Spring Security que valida credenciales.
	 */
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

	/**
	 * Registra un usuario nuevo con rol USER.
	 *
	 * @param request datos enviados por el cliente: username, email y password.
	 * @return AuthResponse con token JWT y datos publicos del usuario.
	 */
	@Transactional // Ejecuta el registro dentro de una transaccion de base de datos.
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

	/**
	 * Inicia sesion con username o email y password.
	 *
	 * @param request datos de login enviados por el cliente.
	 * @return AuthResponse con token JWT y datos publicos del usuario.
	 */
	@Transactional(readOnly = true) // Solo consulta datos; no modifica la base.
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

	/**
	 * Devuelve los datos publicos del usuario autenticado.
	 *
	 * @return UserResponse sin password.
	 */
	@Transactional(readOnly = true) // Solo consulta datos del usuario autenticado.
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

	/**
	 * Verifica que el username no exista antes de registrar.
	 *
	 * @param username nombre de usuario solicitado.
	 */
	private void validateUsernameIsAvailable(String username) {
		if (userRepository.existsByUsername(username)) {
			throw new DuplicateResourceException("El username ya esta registrado");
		}
	}

	/**
	 * Verifica que el email no exista antes de registrar.
	 *
	 * @param email email solicitado.
	 */
	private void validateEmailIsAvailable(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("El email ya esta registrado");
		}
	}

	/**
	 * Convierte un User interno en respuesta de autenticacion.
	 *
	 * @param user entidad guardada en base de datos.
	 * @param token JWT generado.
	 * @return DTO seguro para devolver al cliente.
	 */
	private AuthResponse toAuthResponse(User user, String token) {
		return new AuthResponse(
				token,
				TOKEN_TYPE,
				user.getUsername(),
				user.getEmail(),
				user.getRole()
		);
	}

	/**
	 * Convierte un User interno en respuesta publica de usuario.
	 *
	 * @param user entidad guardada en base de datos.
	 * @return DTO sin password.
	 */
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
