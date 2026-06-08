package ar.edu.utn.frvm.prode.match.entity;

import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.team.entity.Team;
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
 * Entidad JPA que representa un partido deportivo dentro de una fecha.
 *
 * Un partido tiene una fecha, un equipo local, un equipo visitante, horario y estado.
 */
@Entity // Indica que Hibernate debe guardar esta clase en una tabla.
@Table(name = "matches") // Tabla donde se almacenan los partidos.
@Getter // Lombok genera getters para leer los campos desde services y DTOs.
@Setter // Lombok genera setters para que JPA y los services actualicen campos controlados.
@NoArgsConstructor // JPA necesita constructor vacio para materializar entidades.
public class Match {

	@Id // Clave primaria del partido.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL genera el id automaticamente.
	private Long id;

	/**
	 * Relacion JPA muchos-a-uno: muchos partidos pertenecen a una misma fecha.
	 *
	 * FetchType.LAZY evita cargar la fecha completa hasta que realmente se necesite,
	 * lo que mejora rendimiento cuando se listan muchos partidos.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false) // Cada partido debe pertenecer obligatoriamente a una fecha.
	@JoinColumn(name = "match_day_id", nullable = false) // Columna FK que apunta a match_days.id.
	private MatchDay matchDay;

	/**
	 * Relacion JPA muchos-a-uno: muchos partidos pueden usar el mismo equipo como local.
	 *
	 * No se usa cascada porque borrar o guardar un partido no debe borrar ni crear equipos automaticamente.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false) // El equipo local es obligatorio.
	@JoinColumn(name = "home_team_id", nullable = false) // Columna FK que apunta a teams.id.
	private Team homeTeam;

	/**
	 * Relacion JPA muchos-a-uno: muchos partidos pueden usar el mismo equipo como visitante.
	 *
	 * Es una relacion separada de homeTeam porque el mismo partido necesita distinguir local y visitante.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false) // El equipo visitante es obligatorio.
	@JoinColumn(name = "away_team_id", nullable = false) // Columna FK que apunta a teams.id.
	private Team awayTeam;

	@Column(nullable = false) // Horario obligatorio del partido.
	private Instant startTime;

	@Enumerated(EnumType.STRING) // Guarda POR_JUGARSE, EN_JUEGO o FINALIZADO como texto.
	@Column(nullable = false, length = 20) // Todo partido debe tener estado.
	private MatchStatus status;

	@Column // Puede quedar null en esta etapa porque todavia no se cargan resultados reales.
	private Integer homeGoals;

	@Column // Puede quedar null en esta etapa porque todavia no se cargan resultados reales.
	private Integer awayGoals;

	@Enumerated(EnumType.STRING) // Guarda LOCAL, EMPATE o VISITANTE como texto cuando exista resultado.
	@Column(length = 20) // Puede quedar null hasta la etapa de resultados.
	private ResultTrend resultTrend;

	@Column(nullable = false, updatable = false) // Instante automatico de creacion, no editable.
	private Instant createdAt;

	@Column(nullable = false) // Instante automatico de ultima modificacion.
	private Instant updatedAt;

	/**
	 * Crea un partido nuevo con las relaciones obligatorias y horario.
	 *
	 * @param matchDay fecha a la que pertenece el partido.
	 * @param homeTeam equipo local.
	 * @param awayTeam equipo visitante.
	 * @param startTime horario del partido expresado como Instant en UTC.
	 */
	public Match(MatchDay matchDay, Team homeTeam, Team awayTeam, Instant startTime) {
		this.matchDay = matchDay;
		this.homeTeam = homeTeam;
		this.awayTeam = awayTeam;
		this.startTime = startTime;
	}

	/**
	 * Inicializa campos automaticos antes del primer INSERT.
	 *
	 * No recibe parametros.
	 * Asigna estado inicial POR_JUGARSE y timestamps en UTC usando Instant.
	 * No devuelve nada porque modifica la entidad antes de persistirla.
	 */
	@PrePersist // JPA ejecuta este metodo antes de insertar el partido.
	public void prePersist() {
		if (this.status == null) {
			this.status = MatchStatus.POR_JUGARSE;
		}
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	/**
	 * Actualiza el timestamp de modificacion antes de cada UPDATE.
	 *
	 * No recibe parametros.
	 * Guarda el instante exacto de actualizacion en UTC.
	 * No devuelve nada porque solo modifica updatedAt.
	 */
	@PreUpdate // JPA ejecuta este metodo antes de actualizar el registro.
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
