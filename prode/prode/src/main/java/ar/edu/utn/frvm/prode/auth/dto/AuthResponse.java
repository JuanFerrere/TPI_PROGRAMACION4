package ar.edu.utn.frvm.prode.auth.dto;

import ar.edu.utn.frvm.prode.user.entity.Role;

public record AuthResponse(
		String token,
		String tokenType,
		String username,
		String email,
		Role role
) {
}
