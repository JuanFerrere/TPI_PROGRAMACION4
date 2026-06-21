package ar.edu.utn.frvm.prode.tournament.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad JPA que representa un torneo del Prode.
 *
 * En esta etapa el torneo existe de forma aislada. Todavia no se relaciona con
 * equipos, fechas, partidos, pronosticos, rankings ni grupos.
 */
@Entity
@Table(
		name = "tournaments",
		uniqueConstraints = @UniqueConstraint(name = "uk_tournaments_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Tournament {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TournamentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TournamentFormat format;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	/**
	 * Crea un torneo nuevo con datos basicos.
	 *
	 * @param name nombre visible del torneo.
	 * @param description descripcion opcional.
	 */
	public Tournament(String name, String description, TournamentFormat format) {
		this.name = name;
		this.description = description;
		this.format = format;
	}

	/**
	 * Inicializa estado y timestamps antes de insertar.
	 */
	@PrePersist
	public void prePersist() {
		if (this.status == null) {
			this.status = TournamentStatus.DRAFT;
		}
		if (this.format == null) {
			this.format = TournamentFormat.LEAGUE;
		}

		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	/**
	 * Actualiza el timestamp de modificacion antes de cada update.
	 */
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
