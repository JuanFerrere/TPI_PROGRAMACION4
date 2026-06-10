package ar.edu.utn.frvm.prode.group.repository;

import ar.edu.utn.frvm.prode.group.entity.PrivateGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateGroupRepository extends JpaRepository<PrivateGroup, Long> {
	Optional<PrivateGroup> findByInviteCode(String inviteCode);

	boolean existsByInviteCode(String inviteCode);
}
