package ar.edu.utn.frvm.prode.group.dto;

import java.time.Instant;
import java.util.List;

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
