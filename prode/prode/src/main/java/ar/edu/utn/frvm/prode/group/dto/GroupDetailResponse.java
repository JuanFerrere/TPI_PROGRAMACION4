package ar.edu.utn.frvm.prode.group.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO de salida que representa un grupo privado con su lista completa de miembros.
 *
 * Solo accesible para usuarios que ya pertenecen al grupo. Incluye el código de
 * invitación para que los miembros puedan compartirlo con otros usuarios.
 */
public record GroupDetailResponse(

		Long id,
		String name,
		String inviteCode,
		Long ownerId,
		String ownerUsername,
		Integer membersCount,
		List<GroupMemberResponse> members,
		Instant createdAt

) {}
