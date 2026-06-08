package ar.edu.utn.frvm.prode.matchday.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.dto.MatchDayCreateRequest;
import ar.edu.utn.frvm.prode.matchday.dto.MatchDayResponse;
import ar.edu.utn.frvm.prode.matchday.dto.MatchDayUpdateRequest;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de fechas o jornadas.
 *
 * Administra MatchDay y recalcula su estado segun los partidos que contiene.
 */
@Service // Spring registra esta clase como service de dominio.
public class MatchDayService {

	private final MatchDayRepository matchDayRepository;
	private final MatchRepository matchRepository;

	/**
	 * Constructor con repositorios necesarios.
	 *
	 * @param matchDayRepository repositorio para fechas.
	 * @param matchRepository repositorio para consultar partidos asociados.
	 */
	public MatchDayService(MatchDayRepository matchDayRepository, MatchRepository matchRepository) {
		this.matchDayRepository = matchDayRepository;
		this.matchRepository = matchRepository;
	}

	/**
	 * Crea una fecha nueva con estado inicial PROGRAMADA.
	 *
	 * @param request DTO de entrada con el nombre de la fecha.
	 * @return DTO de salida de la fecha creada.
	 */
	@Transactional // Guarda la fecha dentro de una transaccion.
	public MatchDayResponse createMatchDay(MatchDayCreateRequest request) {
		String name = normalizeName(request.name());

		// Regla de negocio: no se permiten fechas duplicadas por nombre, ignorando mayusculas/minusculas.
		if (matchDayRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe una fecha con ese nombre");
		}

		MatchDay matchDay = new MatchDay(name);
		matchDay.setStatus(MatchDayStatus.PROGRAMADA);
		MatchDay savedMatchDay = matchDayRepository.save(matchDay);
		return toResponse(savedMatchDay);
	}

	/**
	 * Modifica el nombre de una fecha.
	 *
	 * @param id identificador de la fecha.
	 * @param request DTO de entrada con el nuevo nombre.
	 * @return DTO de salida actualizado.
	 */
	@Transactional // La validacion y el update se ejecutan de forma atomica.
	public MatchDayResponse updateMatchDay(Long id, MatchDayUpdateRequest request) {
		MatchDay matchDay = getMatchDayEntityById(id);
		String name = normalizeName(request.name());

		// Regla de negocio: una fecha solo se modifica si sigue PROGRAMADA.
		if (matchDay.getStatus() != MatchDayStatus.PROGRAMADA) {
			throw new BusinessRuleException("No se puede modificar una fecha que no esta PROGRAMADA");
		}

		// Regla de negocio: no se modifica una fecha si ya tiene partidos asociados.
		if (matchRepository.existsByMatchDayId(id)) {
			throw new BusinessRuleException("No se puede modificar una fecha que ya tiene partidos asociados");
		}

		// Validacion de duplicado: permite conservar el mismo nombre, pero no tomar el de otra fecha.
		if (!matchDay.getName().equalsIgnoreCase(name) && matchDayRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe una fecha con ese nombre");
		}

		matchDay.setName(name);
		return toResponse(matchDay);
	}

	/**
	 * Elimina una fecha si esta PROGRAMADA y sin partidos.
	 *
	 * @param id identificador de la fecha a eliminar.
	 */
	@Transactional // La validacion y el borrado se ejecutan juntos.
	public void deleteMatchDay(Long id) {
		MatchDay matchDay = getMatchDayEntityById(id);

		// Regla de negocio: no se elimina una fecha en juego o finalizada.
		if (matchDay.getStatus() != MatchDayStatus.PROGRAMADA) {
			throw new BusinessRuleException("No se puede eliminar una fecha que no esta PROGRAMADA");
		}

		// Regla de negocio: no se elimina una fecha con partidos para no dejar partidos huerfanos.
		if (matchRepository.existsByMatchDayId(id)) {
			throw new BusinessRuleException("No se puede eliminar una fecha que tiene partidos asociados");
		}

		matchDayRepository.delete(matchDay);
	}

	/**
	 * Lista todas las fechas.
	 *
	 * No recibe parametros.
	 * @return lista de fechas como DTOs.
	 */
	@Transactional(readOnly = true) // Consulta sin modificar estado.
	public List<MatchDayResponse> getAllMatchDays() {
		return matchDayRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Lista fechas filtradas por estado.
	 *
	 * @param status estado recibido por query param.
	 * @return lista de fechas con ese estado.
	 */
	@Transactional(readOnly = true) // Consulta filtrada sin cambios en base.
	public List<MatchDayResponse> getMatchDaysByStatus(MatchDayStatus status) {
		return matchDayRepository.findByStatus(status)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Obtiene una fecha por id.
	 *
	 * @param id identificador de la fecha.
	 * @return DTO de la fecha encontrada.
	 */
	@Transactional(readOnly = true) // Solo consulta la fecha solicitada.
	public MatchDayResponse getMatchDayById(Long id) {
		return toResponse(getMatchDayEntityById(id));
	}

	/**
	 * Recalcula el estado de una fecha segun sus partidos.
	 *
	 * @param matchDayId id de la fecha a recalcular.
	 * @return DTO de la fecha con el estado actualizado.
	 */
	@Transactional // Puede modificar el estado de la fecha.
	public MatchDayResponse refreshStatusByMatchDayId(Long matchDayId) {
		MatchDay matchDay = getMatchDayEntityById(matchDayId);
		List<Match> matches = matchRepository.findByMatchDayIdOrderByStartTimeAsc(matchDayId);

		MatchDayStatus newStatus = MatchDayStatus.PROGRAMADA;

		if (!matches.isEmpty()) {
			boolean allFinished = matches.stream().allMatch(match -> match.getStatus() == MatchStatus.FINALIZADO);
			boolean anyInGame = matches.stream().anyMatch(match -> match.getStatus() == MatchStatus.EN_JUEGO);
			boolean allScheduled = matches.stream().allMatch(match -> match.getStatus() == MatchStatus.POR_JUGARSE);

			// Regla de negocio: si todos terminaron, la fecha queda FINALIZADA.
			if (allFinished) {
				newStatus = MatchDayStatus.FINALIZADA;
			// Regla de negocio: si al menos uno esta en juego, la fecha queda EN_JUEGO.
			} else if (anyInGame) {
				newStatus = MatchDayStatus.EN_JUEGO;
			// Regla de negocio: si todos estan por jugarse, la fecha sigue PROGRAMADA.
			} else if (allScheduled) {
				newStatus = MatchDayStatus.PROGRAMADA;
			} else {
				// Preparacion para futuras etapas: mezcla de finalizados y pendientes significa fecha ya iniciada.
				newStatus = MatchDayStatus.EN_JUEGO;
			}
		}

		matchDay.setStatus(newStatus);
		return toResponse(matchDay);
	}

	/**
	 * Busca una entidad MatchDay por id.
	 *
	 * @param id identificador de la fecha.
	 * @return entidad MatchDay encontrada.
	 */
	private MatchDay getMatchDayEntityById(Long id) {
		return matchDayRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada"));
	}

	/**
	 * Normaliza nombres antes de guardar o comparar.
	 *
	 * @param name nombre recibido desde el cliente.
	 * @return nombre sin espacios al inicio o final.
	 */
	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException("El nombre de la fecha es obligatorio");
		}
		return name.trim();
	}

	/**
	 * Convierte entidad MatchDay a DTO de salida.
	 *
	 * @param matchDay entidad JPA interna.
	 * @return DTO seguro para devolver al cliente.
	 */
	private MatchDayResponse toResponse(MatchDay matchDay) {
		return new MatchDayResponse(
				matchDay.getId(),
				matchDay.getName(),
				matchDay.getStatus(),
				matchDay.getCreatedAt()
		);
	}
}
