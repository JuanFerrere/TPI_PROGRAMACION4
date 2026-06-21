package ar.edu.utn.frvm.prode.matchday.entity;

import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad JPA que representa una fecha o jornada del torneo.
 *
 * Una fecha agrupa partidos, pero no exponemos una lista de partidos dentro de esta entidad
 * en las respuestas para evitar respuestas gigantes o recursividad JSON.
 */
@Entity // Indica que esta clase se guarda y consulta con JPA/Hibernate.
@Table(name = "match_days") // Nombre fisico de la tabla para fechas o jornadas.
@Getter // Lombok genera getters para leer campos desde services y mapeos a DTO.
@Setter // Lombok genera setters controlados por la capa service.
@NoArgsConstructor // Constructor vacio requerido por JPA.
public class MatchDay {

	@Id // Clave primaria de la fecha.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // La base genera el id automaticamente.
	private Long id;

	@Column(nullable = false, length = 100) // Nombre obligatorio y con longitud maxima.
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tournament_id")
	private Tournament tournament;

	@Column(name = "order_number")
	private Integer orderNumber;

	@Enumerated(EnumType.STRING) // Guarda PROGRAMADA, EN_JUEGO o FINALIZADA como texto legible.
	@Column(nullable = false, length = 20) // Toda fecha debe tener un estado.
	private MatchDayStatus status;

	@Column(nullable = false, updatable = false) // Fecha de creacion automatica, no editable por el cliente.
	private Instant createdAt;

	@Column
	private Instant updatedAt;

	/**
	 * Crea una fecha nueva con nombre.
	 *
	 * @param name nombre visible de la fecha o jornada.
	 */
	public MatchDay(String name) {
		this.name = name;
	}

	public MatchDay(String name, Tournament tournament, Integer orderNumber) {
		this.name = name;
		this.tournament = tournament;
		this.orderNumber = orderNumber;
	}

	/**
	 * Inicializa campos automaticos antes del primer guardado.
	 *
	 * No recibe parametros.
	 * Asigna PROGRAMADA como estado inicial y guarda createdAt en UTC con Instant.
	 * No devuelve nada porque solo prepara la entidad antes del INSERT.
	 */
	@PrePersist // JPA ejecuta este metodo antes de insertar la fecha en la base.
	public void prePersist() {
		if (this.status == null) {
			this.status = MatchDayStatus.PROGRAMADA;
		}
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
