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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "private_groups")
@Getter
@Setter
@NoArgsConstructor
public class PrivateGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true, length = 50)
	private String inviteCode;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public PrivateGroup(String name, String inviteCode, User owner) {
		this.name = name;
		this.inviteCode = inviteCode;
		this.owner = owner;
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
