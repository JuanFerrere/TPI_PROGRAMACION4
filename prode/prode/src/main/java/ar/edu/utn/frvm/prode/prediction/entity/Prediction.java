package ar.edu.utn.frvm.prode.prediction.entity;

import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.user.entity.User;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad JPA que representa el pronostico de un usuario para un partido.
 *
 * En el Prode, cada usuario puede cargar un solo pronostico por partido. Si el
 * usuario vuelve a enviar un pronostico para el mismo partido, el sistema
 * actualiza el registro existente en lugar de crear uno duplicado.
 */
@Entity // Indica que Hibernate debe guardar esta clase en una tabla de la base de datos.
@Table(
		name = "predictions",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_predictions_user_match",
				columnNames = {"user_id", "match_id"}
		) // Refuerza en base la regla: un usuario solo puede pronosticar una vez cada partido.
)
@Getter // Lombok genera getters para leer los campos desde services y DTOs.
@Setter // Lombok genera setters usados por JPA y por la logica de negocio controlada.
@NoArgsConstructor // JPA necesita un constructor vacio para reconstruir entidades desde la base.
public class Prediction {

	@Id // Clave primaria del pronostico.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL genera el id automaticamente.
	private Long id;

	/**
	 * Relacion muchos-a-uno: muchos pronosticos pueden pertenecer al mismo usuario.
	 *
	 * FetchType.LAZY evita cargar todos los datos del usuario hasta que realmente se necesitan.
	 * No se usa cascada porque crear un pronostico no debe crear ni borrar usuarios.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/**
	 * Relacion muchos-a-uno: muchos usuarios pueden pronosticar el mismo partido.
	 *
	 * El partido ya existe desde Etapa 3 y el pronostico solo lo referencia.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@Column(nullable = false) // Goles pronosticados para el equipo local.
	private Integer predictedHomeGoals;

	@Column(nullable = false) // Goles pronosticados para el equipo visitante.
	private Integer predictedAwayGoals;

	@Enumerated(EnumType.STRING) // Guarda LOCAL, EMPATE o VISITANTE como texto legible.
	@Column(nullable = false, length = 20)
	private ResultTrend predictedTrend;

	@Column(nullable = false) // Queda preparado para Etapa 5, cuando se calculen puntos reales.
	private Integer points;

	@Column(nullable = false) // Queda preparado para Etapa 5, cuando se determine si acerto exacto.
	private Boolean exactHit;

	@Column(nullable = false, updatable = false) // Instante exacto de creacion en UTC.
	private Instant createdAt;

	@Column(nullable = false) // Instante exacto de ultima modificacion en UTC.
	private Instant updatedAt;

	/**
	 * Constructor usado por el service cuando se crea un pronostico nuevo.
	 *
	 * @param user usuario autenticado que realiza el pronostico.
	 * @param match partido que se pronostica.
	 */
	public Prediction(User user, Match match) {
		this.user = user;
		this.match = match;
	}

	/**
	 * Actualiza los valores pronosticados.
	 *
	 * El service calcula la tendencia a partir de los goles y la entidad guarda
	 * ese resultado para facilitar consultas futuras.
	 *
	 * @param predictedHomeGoals goles pronosticados para el local.
	 * @param predictedAwayGoals goles pronosticados para el visitante.
	 * @param predictedTrend tendencia calculada: LOCAL, EMPATE o VISITANTE.
	 */
	public void updatePrediction(
			Integer predictedHomeGoals,
			Integer predictedAwayGoals,
			ResultTrend predictedTrend
	) {
		this.predictedHomeGoals = predictedHomeGoals;
		this.predictedAwayGoals = predictedAwayGoals;
		this.predictedTrend = predictedTrend;
	}

	/**
	 * Inicializa campos automaticos antes del primer INSERT.
	 *
	 * points y exactHit comienzan en cero/falso porque el partido todavia no
	 * tiene resultado real. Esos campos quedan preparados para Etapa 5.
	 */
	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;

		if (this.points == null) {
			this.points = 0;
		}

		if (this.exactHit == null) {
			this.exactHit = false;
		}
	}

	/**
	 * Actualiza el timestamp cada vez que el pronostico cambia.
	 */
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
