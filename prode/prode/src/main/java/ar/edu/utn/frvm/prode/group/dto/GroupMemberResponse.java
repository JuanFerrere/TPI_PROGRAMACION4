package ar.edu.utn.frvm.prode.group.dto;

import java.time.Instant;

/**
 * DTO de salida que representa a un miembro dentro de un grupo.
 *
 * Se incluye en GroupDetailResponse para listar quiénes forman parte del grupo.
 * No expone datos sensibles como email ni contraseña.
 */
public record GroupMemberResponse(

		Long userId,
		String username,
		Instant joinedAt

) {}
