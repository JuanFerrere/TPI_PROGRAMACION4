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

/**
 * Entidad JPA que representa la membresía de un usuario en un grupo privado.
 *
 * Actúa como tabla intermedia entre PrivateGroup y User. La restricción única
 * sobre (group_id, user_id) garantiza que un usuario no pueda estar dos veces
 * en el mismo grupo.
 *
 * Tabla: group_members
 */
@Entity // Indica que Hibernate debe guardar esta clase en una tabla de la base de datos.
@Table(
		name = "group_members",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_group_members_group_user",
				columnNames = {"group_id", "user_id"}
		) // Refuerza en base que un usuario no puede pertenecer dos veces al mismo grupo.
)
@Getter // Lombok genera getters para todos los campos.
@Setter // Lombok genera setters usados por JPA y el service.
@NoArgsConstructor // JPA necesita un constructor vacío para reconstruir entidades desde la base.
public class GroupMember {

	@Id // Clave primaria de la membresía.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL genera el id automáticamente.
	private Long id;

	/**
	 * Grupo al que pertenece esta membresía.
	 *
	 * FetchType.LAZY evita cargar todos los datos del grupo hasta que se necesiten.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "group_id", nullable = false)
	private PrivateGroup group;

	/**
	 * Usuario miembro del grupo.
	 *
	 * FetchType.LAZY evita cargar todos los datos del usuario hasta que se necesiten.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, updatable = false) // Momento en que el usuario se unió al grupo.
	private Instant joinedAt;

	/**
	 * Constructor usado por el service al agregar un miembro a un grupo.
	 *
	 * @param group grupo al que se une el usuario.
	 * @param user  usuario que se une al grupo.
	 */
	public GroupMember(PrivateGroup group, User user) {
		this.group = group;
		this.user = user;
	}

	/**
	 * Completa el timestamp de ingreso antes del primer INSERT.
	 */
	@PrePersist // JPA ejecuta este método antes de guardar por primera vez la entidad.
	public void prePersist() {
		this.joinedAt = Instant.now();
	}
}
