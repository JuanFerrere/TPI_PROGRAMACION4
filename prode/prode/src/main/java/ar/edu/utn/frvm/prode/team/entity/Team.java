package ar.edu.utn.frvm.prode.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad JPA que representa un equipo participante del Prode.
 *
 * Un equipo puede aparecer en muchos partidos, ya sea como local o como visitante.
 */
@Entity // Indica que Hibernate debe mapear esta clase a una tabla de PostgreSQL.
@Table(
		name = "teams", // Define que los registros se guardan en la tabla teams.
		uniqueConstraints = @UniqueConstraint(name = "uk_teams_name", columnNames = "name") // Evita nombres duplicados en base.
)
@Getter // Lombok genera getters para que services y DTOs puedan leer los campos.
@Setter // Lombok genera setters usados por JPA y por la logica de negocio controlada.
@NoArgsConstructor // JPA necesita un constructor vacio para reconstruir entidades desde la base.
public class Team {

	@Id // Marca este campo como clave primaria.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL genera el id automaticamente al insertar.
	private Long id;

	@Column(nullable = false, unique = true, length = 100) // Nombre obligatorio, unico y limitado para evitar datos demasiado largos.
	private String name;

	@Column(nullable = false, updatable = false) // Se completa al crear y no debe modificarse luego.
	private Instant createdAt;

	/**
	 * Crea una entidad Team nueva antes de persistirla.
	 *
	 * @param name nombre visible del equipo.
	 */
	public Team(String name) {
		this.name = name;
	}

	/**
	 * Completa datos automaticos antes del primer INSERT.
	 *
	 * No recibe parametros.
	 * Guarda el instante exacto de creacion en UTC usando Instant.
	 * No devuelve nada porque modifica la entidad antes de que JPA la inserte.
	 */
	@PrePersist // JPA ejecuta este metodo automaticamente antes de guardar por primera vez.
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
