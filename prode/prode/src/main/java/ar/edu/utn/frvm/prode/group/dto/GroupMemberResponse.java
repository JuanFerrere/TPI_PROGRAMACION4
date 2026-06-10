package ar.edu.utn.frvm.prode.group.dto;

import java.time.Instant;

public record GroupMemberResponse(

		Long userId,
		String username,
		Instant joinedAt

) {}
