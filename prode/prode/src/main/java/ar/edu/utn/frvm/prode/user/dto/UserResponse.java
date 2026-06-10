package ar.edu.utn.frvm.prode.user.dto;

import ar.edu.utn.frvm.prode.user.entity.Role;

import java.time.Instant;

public record UserResponse(
		Long id,
		String username,
		String email,
		Role role,
		Instant createdAt
) {
}
