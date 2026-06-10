package ar.edu.utn.frvm.prode.group.repository;

import ar.edu.utn.frvm.prode.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
	boolean existsByGroupIdAndUserId(Long groupId, Long userId);

	Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

	List<GroupMember> findByUserId(Long userId);

	List<GroupMember> findByGroupId(Long groupId);

	long countByGroupId(Long groupId);

	void deleteByGroupIdAndUserId(Long groupId, Long userId);
}
