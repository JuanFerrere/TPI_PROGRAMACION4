package ar.edu.utn.frvm.prode.prediction.repository;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para pronosticos.
 *
 * Spring Data JPA genera consultas a partir del nombre de los metodos. Cuando
 * se escribe "UserId" o "MatchStatus", Spring navega las relaciones:
 * prediction.user.id y prediction.match.status.
 */
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

	/**
	 * Busca el pronostico de un usuario para un partido.
	 *
	 * Este metodo permite aplicar la regla upsert: si existe, se actualiza; si
	 * no existe, se crea uno nuevo.
	 *
	 * @param userId id del usuario autenticado.
	 * @param matchId id del partido.
	 * @return Optional con el pronostico si existe.
	 */
	Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

	/**
	 * Lista todos los pronosticos de un usuario ordenados por horario del partido.
	 *
	 * @param userId id del usuario autenticado.
	 * @return lista de pronosticos propios.
	 */
	List<Prediction> findByUserIdOrderByMatchStartTimeAsc(Long userId);

	/**
	 * Lista pronosticos propios filtrados por estado del partido.
	 *
	 * @param userId id del usuario autenticado.
	 * @param status estado del partido usado como filtro.
	 * @return lista filtrada y ordenada por horario del partido.
	 */
	List<Prediction> findByUserIdAndMatchStatusOrderByMatchStartTimeAsc(Long userId, MatchStatus status);

	/**
	 * Lista pronosticos de un partido ordenados por momento de creacion.
	 *
	 * Solo debe usarse despues de validar que ya cerro el periodo de carga.
	 *
	 * @param matchId id del partido.
	 * @return pronosticos cargados para ese partido.
	 */
	List<Prediction> findByMatchIdOrderByCreatedAtAsc(Long matchId);

	/**
	 * Verifica si un partido tiene pronosticos asociados.
	 *
	 * Queda disponible para reglas futuras de integridad.
	 *
	 * @param matchId id del partido.
	 * @return true si existe al menos un pronostico para ese partido.
	 */
	boolean existsByMatchId(Long matchId);

	/**
	 * Lista pronosticos de un conjunto de usuarios.
	 *
	 * Se usa en el ranking por grupo para obtener los pronosticos de todos los
	 * miembros del grupo en una sola consulta. Evita N consultas individuales.
	 *
	 * @param userIds lista de ids de usuarios miembros del grupo.
	 * @return pronosticos pertenecientes a esos usuarios.
	 */
	List<Prediction> findByUserIdIn(List<Long> userIds);
}
