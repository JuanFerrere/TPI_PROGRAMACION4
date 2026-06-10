package ar.edu.utn.frvm.prode.group.dto;

import java.time.Instant;

public record GroupResponse(

		Long id,
		String name,
		String inviteCode,
		Long ownerId,
		String ownerUsername,
		Integer membersCount,
		Instant createdAt

) {}
