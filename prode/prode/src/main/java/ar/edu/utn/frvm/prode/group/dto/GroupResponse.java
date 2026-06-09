package ar.edu.utn.frvm.prode.group.dto;

import java.time.Instant;

/**
 * DTO de salida que representa un grupo privado en forma resumida.
 *
 * Se usa en la lista de grupos del usuario y al crear o unirse a un grupo.
 * No incluye la lista completa de miembros para mantener la respuesta liviana.
 * El detalle completo con miembros se obtiene mediante GroupDetailResponse.
 */
public record GroupResponse(

		Long id,
		String name,
		String inviteCode,
		Long ownerId,
		String ownerUsername,
		Integer membersCount,
		Instant createdAt

) {}
