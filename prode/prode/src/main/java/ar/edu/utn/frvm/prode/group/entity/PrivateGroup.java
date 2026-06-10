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

/**
 * Entidad JPA que representa un grupo privado de pronósticos.
 *
 * Un grupo permite que un conjunto de usuarios compita entre sí. El acceso
 * al grupo se controla mediante un código de invitación único generado por el
 * backend al momento de la creación. El usuario creador queda como owner.
 *
 * Tabla: private_groups
 */
@Entity // Indica que Hibernate debe guardar esta clase en una tabla de la base de datos.
@Table(name = "private_groups") // Nombre explícito para evitar conflictos con la palabra reservada "group".
@Getter // Lombok genera getters para todos los campos.
@Setter // Lombok genera setters usados por JPA y el service.
@NoArgsConstructor // JPA necesita un constructor vacío para reconstruir entidades desde la base.
public class PrivateGroup {

	@Id // Clave primaria del grupo.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL genera el id automáticamente.
	private Long id;

	@Column(nullable = false, length = 100) // Nombre obligatorio con límite de caracteres.
	private String name;

	@Column(nullable = false, unique = true, length = 50) // Código único: evita colisiones entre grupos.
	private String inviteCode;

	/**
	 * Usuario que creó el grupo. Queda como owner permanente.
	 *
	 * FetchType.LAZY evita cargar todos los datos del usuario hasta que se necesiten.
	 * No hay cascada porque gestionar el usuario no es responsabilidad del grupo.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false, updatable = false) // Timestamp de creación: se fija al insertar y no cambia.
	private Instant createdAt;

	/**
	 * Constructor usado por el service al crear un grupo nuevo.
	 *
	 * @param name      nombre del grupo elegido por el usuario.
	 * @param inviteCode código de invitación generado por el backend.
	 * @param owner     usuario creador del grupo.
	 */
	public PrivateGroup(String name, String inviteCode, User owner) {
		this.name = name;
		this.inviteCode = inviteCode;
		this.owner = owner;
	}

	/**
	 * Completa el timestamp de creación antes del primer INSERT.
	 */
	@PrePersist // JPA ejecuta este método antes de guardar por primera vez la entidad.
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
