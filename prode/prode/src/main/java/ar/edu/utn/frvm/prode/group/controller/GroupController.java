package ar.edu.utn.frvm.prode.group.controller;

import ar.edu.utn.frvm.prode.group.dto.GroupCreateRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupDetailResponse;
import ar.edu.utn.frvm.prode.group.dto.GroupJoinRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupResponse;
import ar.edu.utn.frvm.prode.group.service.GroupService;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated()")
	public GroupResponse createGroup(
			@Valid @RequestBody GroupCreateRequest request,
			Authentication authentication
	) {
		return groupService.createGroup(request, authentication);
	}

	@PostMapping("/join")
	@PreAuthorize("isAuthenticated()")
	public GroupResponse joinGroup(
			@Valid @RequestBody GroupJoinRequest request,
			Authentication authentication
	) {
		return groupService.joinGroup(request, authentication);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public List<GroupResponse> getMyGroups(Authentication authentication) {
		return groupService.getMyGroups(authentication);
	}

	@GetMapping("/{groupId}")
	@PreAuthorize("isAuthenticated()")
	public GroupDetailResponse getGroupDetail(
			@PathVariable Long groupId,
			Authentication authentication
	) {
		return groupService.getGroupDetail(groupId, authentication);
	}

	@DeleteMapping("/{groupId}/members/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated()")
	public void leaveGroup(
			@PathVariable Long groupId,
			Authentication authentication
	) {
		groupService.leaveGroup(groupId, authentication);
	}

	@GetMapping("/{groupId}/ranking")
	@PreAuthorize("isAuthenticated()")
	public List<RankingResponse> getGroupRanking(
			@PathVariable Long groupId,
			Authentication authentication
	) {
		return groupService.getGroupRanking(groupId, authentication);
	}
}
