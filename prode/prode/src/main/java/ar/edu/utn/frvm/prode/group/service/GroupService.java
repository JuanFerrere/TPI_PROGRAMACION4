package ar.edu.utn.frvm.prode.group.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.group.dto.GroupCreateRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupDetailResponse;
import ar.edu.utn.frvm.prode.group.dto.GroupJoinRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupMemberResponse;
import ar.edu.utn.frvm.prode.group.dto.GroupResponse;
import ar.edu.utn.frvm.prode.group.entity.GroupMember;
import ar.edu.utn.frvm.prode.group.entity.PrivateGroup;
import ar.edu.utn.frvm.prode.group.repository.GroupMemberRepository;
import ar.edu.utn.frvm.prode.group.repository.PrivateGroupRepository;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GroupService {
	private final PrivateGroupRepository privateGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final PredictionRepository predictionRepository;
	private final UserRepository userRepository;

	public GroupService(
			PrivateGroupRepository privateGroupRepository,
			GroupMemberRepository groupMemberRepository,
			PredictionRepository predictionRepository,
			UserRepository userRepository
	) {
		this.privateGroupRepository = privateGroupRepository;
		this.groupMemberRepository = groupMemberRepository;
		this.predictionRepository = predictionRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public GroupResponse createGroup(GroupCreateRequest request, Authentication authentication) {
		User owner = getAuthenticatedUser(authentication);

		String inviteCode = generateInviteCode();

		PrivateGroup group = new PrivateGroup(request.name(), inviteCode, owner);
		PrivateGroup savedGroup = privateGroupRepository.save(group);

		GroupMember ownerMember = new GroupMember(savedGroup, owner);
		groupMemberRepository.save(ownerMember);

		long membersCount = groupMemberRepository.countByGroupId(savedGroup.getId());
		return toGroupResponse(savedGroup, (int) membersCount);
	}

	@Transactional
	public GroupResponse joinGroup(GroupJoinRequest request, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = privateGroupRepository.findByInviteCode(request.inviteCode())
				.orElseThrow(() -> new ResourceNotFoundException(
						"No existe un grupo con el codigo de invitacion indicado"
				));

		if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
			throw new BusinessRuleException("El usuario ya pertenece a este grupo");
		}

		GroupMember member = new GroupMember(group, user);
		groupMemberRepository.save(member);

		long membersCount = groupMemberRepository.countByGroupId(group.getId());
		return toGroupResponse(group, (int) membersCount);
	}

	@Transactional(readOnly = true)
	public List<GroupResponse> getMyGroups(Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		List<GroupMember> memberships = groupMemberRepository.findByUserId(user.getId());

		return memberships.stream()
				.map(membership -> {
					PrivateGroup group = membership.getGroup();
					long membersCount = groupMemberRepository.countByGroupId(group.getId());
					return toGroupResponse(group, (int) membersCount);
				})
				.toList();
	}

	@Transactional(readOnly = true)
	public GroupDetailResponse getGroupDetail(Long groupId, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = getGroupEntityById(groupId);

		validateIsMember(group.getId(), user.getId(), "No tenes permisos para acceder a este grupo");

		List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());

		List<GroupMemberResponse> memberResponses = members.stream()
				.map(m -> new GroupMemberResponse(
						m.getUser().getId(),
						m.getUser().getUsername(),
						m.getJoinedAt()
				))
				.toList();

		return new GroupDetailResponse(
				group.getId(),
				group.getName(),
				group.getInviteCode(),
				group.getOwner().getId(),
				group.getOwner().getUsername(),
				members.size(),
				memberResponses,
				group.getCreatedAt()
		);
	}

	@Transactional
	public void leaveGroup(Long groupId, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = getGroupEntityById(groupId);

		validateIsMember(group.getId(), user.getId(), "No perteneces a este grupo");

		if (group.getOwner().getId().equals(user.getId())) {
			throw new BusinessRuleException("El creador del grupo no puede abandonar el grupo");
		}

		groupMemberRepository.deleteByGroupIdAndUserId(group.getId(), user.getId());
	}

	@Transactional(readOnly = true)
	public List<RankingResponse> getGroupRanking(Long groupId, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = getGroupEntityById(groupId);

		validateIsMember(group.getId(), user.getId(), "No tenes permisos para ver el ranking de este grupo");

		List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
		List<Long> memberUserIds = members.stream()
				.map(m -> m.getUser().getId())
				.toList();

		Map<Long, UserScoreAccumulator> accumulatorByUser = new LinkedHashMap<>();
		for (GroupMember member : members) {
			User memberUser = member.getUser();
			accumulatorByUser.put(memberUser.getId(),
					new UserScoreAccumulator(memberUser.getId(), memberUser.getUsername()));
		}

		List<Prediction> predictions = memberUserIds.isEmpty()
				? List.of()
				: predictionRepository.findByUserIdIn(memberUserIds);

		for (Prediction prediction : predictions) {
			UserScoreAccumulator accumulator = accumulatorByUser.get(prediction.getUser().getId());
			if (accumulator != null) {
				accumulator.add(prediction);
			}
		}

		Comparator<UserScoreAccumulator> rankingOrder = Comparator
				.comparingInt(UserScoreAccumulator::getTotalPoints).reversed()
				.thenComparing(Comparator.comparingLong(UserScoreAccumulator::getExactHits).reversed())
				.thenComparing(UserScoreAccumulator::getUsername);

		List<UserScoreAccumulator> ordered = accumulatorByUser.values().stream()
				.sorted(rankingOrder)
				.toList();

		List<RankingResponse> ranking = new ArrayList<>();
		int position = 1;
		for (UserScoreAccumulator accumulator : ordered) {
			ranking.add(new RankingResponse(
					position,
					accumulator.getUserId(),
					accumulator.getUsername(),
					accumulator.getTotalPoints(),
					accumulator.getExactHits(),
					accumulator.getPredictionsCount()
			));
			position++;
		}

		return ranking;
	}

	private void validateIsMember(Long groupId, Long userId, String message) {
		if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
			throw new BusinessRuleException(message);
		}
	}

	private PrivateGroup getGroupEntityById(Long id) {
		return privateGroupRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
	}

	private String generateInviteCode() {
		String code;
		do {
			code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
		} while (privateGroupRepository.existsByInviteCode(code));
		return code;
	}

	private User getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Usuario no autenticado");
		}

		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
	}

	private GroupResponse toGroupResponse(PrivateGroup group, int membersCount) {
		return new GroupResponse(
				group.getId(),
				group.getName(),
				group.getInviteCode(),
				group.getOwner().getId(),
				group.getOwner().getUsername(),
				membersCount,
				group.getCreatedAt()
		);
	}

	private static final class UserScoreAccumulator {
		private final Long userId;
		private final String username;
		private int totalPoints;
		private long exactHits;
		private long predictionsCount;

		private UserScoreAccumulator(Long userId, String username) {
			this.userId = userId;
			this.username = username;
		}

		private void add(Prediction prediction) {
			this.totalPoints += prediction.getPoints();
			if (Boolean.TRUE.equals(prediction.getExactHit())) {
				this.exactHits++;
			}
			this.predictionsCount++;
		}

		private Long getUserId() { return userId; }
		private String getUsername() { return username; }
		private int getTotalPoints() { return totalPoints; }
		private long getExactHits() { return exactHits; }
		private long getPredictionsCount() { return predictionsCount; }
	}
}
