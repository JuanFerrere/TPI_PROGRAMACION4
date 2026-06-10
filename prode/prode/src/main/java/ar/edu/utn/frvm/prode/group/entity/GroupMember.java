package ar.edu.utn.frvm.prode.group.entity;

import ar.edu.utn.frvm.prode.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
		name = "group_members",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_group_members_group_user",
				columnNames = {"group_id", "user_id"}
		)
)
@Getter
@Setter
@NoArgsConstructor
public class GroupMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private PrivateGroup group;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, updatable = false)
	private Instant joinedAt;

	public GroupMember(PrivateGroup group, User user) {
		this.group = group;
		this.user = user;
	}

	@PrePersist
	public void prePersist() {
		this.joinedAt = Instant.now();
	}
}
