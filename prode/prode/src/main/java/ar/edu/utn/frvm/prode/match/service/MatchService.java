package ar.edu.utn.frvm.prode.match.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.dto.MatchCreateRequest;
import ar.edu.utn.frvm.prode.match.dto.MatchResponse;
import ar.edu.utn.frvm.prode.match.dto.MatchResultRequest;
import ar.edu.utn.frvm.prode.match.dto.MatchUpdateRequest;
import ar.edu.utn.frvm.prode.match.entity.Match;
import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.match.entity.ResultTrend;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.matchday.service.MatchDayService;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.prediction.service.PredictionScoringService;
import ar.edu.utn.frvm.prode.team.entity.Team;
import ar.edu.utn.frvm.prode.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Service de partidos.
 *
 * Centraliza reglas de negocio: equipos distintos, estados permitidos y recalcule de fecha.
 */
@Service // Spring registra esta clase como service de dominio.
public class MatchService {

	private final MatchRepository matchRepository;
	private final PredictionRepository predictionRepository;
	private final MatchDayRepository matchDayRepository;
	private final TeamRepository teamRepository;
	private final MatchDayService matchDayService;
	private final PredictionScoringService predictionScoringService;

	/**
	 * Constructor con repositorios y services colaboradores.
	 *
	 * @param matchRepository repositorio para partidos.
	 * @param predictionRepository repositorio para validar pronosticos asociados a partidos.
	 * @param matchDayRepository repositorio para validar fechas existentes.
	 * @param teamRepository repositorio para validar equipos existentes.
	 * @param matchDayService service usado para recalcular el estado de una fecha.
	 * @param predictionScoringService service que calcula los puntos de los pronosticos al cargar un resultado.
	 */
	public MatchService(
			MatchRepository matchRepository,
			PredictionRepository predictionRepository,
			MatchDayRepository matchDayRepository,
			TeamRepository teamRepository,
			MatchDayService matchDayService,
			PredictionScoringService predictionScoringService
	) {
		this.matchRepository = matchRepository;
		this.predictionRepository = predictionRepository;
		this.matchDayRepository = matchDayRepository;
		this.teamRepository = teamRepository;
		this.matchDayService = matchDayService;
		this.predictionScoringService = predictionScoringService;
	}

	/**
	 * Crea un partido nuevo en estado POR_JUGARSE.
	 *
	 * @param request DTO con fecha, equipos y horario.
	 * @return DTO del partido creado.
	 */
	@Transactional // La validacion de relaciones y el guardado se ejecutan en una transaccion.
	public MatchResponse createMatch(MatchCreateRequest request) {
		validateDifferentTeams(request.homeTeamId(), request.awayTeamId());

		MatchDay matchDay = getMatchDayEntityById(request.matchDayId());
		validateMatchDayIsScheduled(matchDay);
		Team homeTeam = getTeamEntityById(request.homeTeamId(), "Equipo local no encontrado");
		Team awayTeam = getTeamEntityById(request.awayTeamId(), "Equipo visitante no encontrado");
		Instant startTime = validateStartTime(request.startTime());

		Match match = new Match(matchDay, homeTeam, awayTeam, startTime);
		match.setStatus(MatchStatus.POR_JUGARSE);

		Match savedMatch = matchRepository.save(match);
		matchDayService.refreshStatusByMatchDayId(matchDay.getId());
		return toResponse(savedMatch);
	}

	/**
	 * Modifica un partido existente.
	 *
	 * @param id identificador del partido.
	 * @param request DTO con nuevo local, visitante y horario.
	 * @return DTO actualizado.
	 */
	@Transactional // La validacion de estado y el update se aplican juntos.
	public MatchResponse updateMatch(Long id, MatchUpdateRequest request) {
		Match match = getMatchEntityById(id);

		// Regla de negocio: un partido solo se modifica antes de comenzar.
		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede modificar un partido que ya comenzo");
		}

		// Regla de negocio: cambiar equipos u horario afectaria pronosticos que los usuarios ya cargaron.
		validateMatchHasNoPredictions(
				match.getId(),
				"No se puede modificar el partido porque ya tiene pronosticos asociados"
		);

		validateDifferentTeams(request.homeTeamId(), request.awayTeamId());

		Team homeTeam = getTeamEntityById(request.homeTeamId(), "Equipo local no encontrado");
		Team awayTeam = getTeamEntityById(request.awayTeamId(), "Equipo visitante no encontrado");
		Instant startTime = validateStartTime(request.startTime());

		match.setHomeTeam(homeTeam);
		match.setAwayTeam(awayTeam);
		match.setStartTime(startTime);

		return toResponse(match);
	}

	/**
	 * Cambia un partido de POR_JUGARSE a EN_JUEGO.
	 *
	 * @param id identificador del partido.
	 * @return DTO del partido iniciado.
	 */
	@Transactional // Cambia el partido y recalcula su fecha en la misma transaccion.
	public MatchResponse startMatch(Long id) {
		Match match = getMatchEntityById(id);

		// Regla de negocio: solo un partido pendiente puede iniciarse.
		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede iniciar un partido que no esta POR_JUGARSE");
		}

		match.setStatus(MatchStatus.EN_JUEGO);
		Match savedMatch = matchRepository.save(match);

		// Regla de negocio: la fecha no se edita manualmente; se recalcula desde sus partidos.
		matchDayService.refreshStatusByMatchDayId(savedMatch.getMatchDay().getId());

		return toResponse(savedMatch);
	}

	/**
	 * Carga el resultado real de un partido EN_JUEGO y dispara el calculo de puntos.
	 *
	 * Este metodo es el cierre del ciclo de vida de un partido. El cliente (ADMIN)
	 * solo envia los goles reales; todo lo demas lo decide el backend:
	 *  1. Valida que el partido este EN_JUEGO.
	 *  2. Calcula la tendencia real (LOCAL, EMPATE o VISITANTE) a partir de los goles.
	 *  3. Guarda goles y tendencia, y pasa el partido a FINALIZADO.
	 *  4. Recalcula los puntos de TODOS los pronosticos del partido.
	 *  5. Recalcula el estado de la fecha contenedora.
	 *
	 * @param id identificador del partido.
	 * @param request goles reales del local y del visitante.
	 * @return DTO del partido finalizado con su resultado.
	 */
	@Transactional // Resultado, finalizacion, puntaje y recalculo de fecha forman una unica unidad de trabajo.
	public MatchResponse loadResult(Long id, MatchResultRequest request) {
		Match match = getMatchEntityById(id);

		// Regla de negocio: solo se cargan resultados de partidos EN_JUEGO. Se distinguen los dos casos invalidos
		// para devolver un mensaje claro segun el estado actual del partido.
		if (match.getStatus() == MatchStatus.FINALIZADO) {
			throw new BusinessRuleException("No se puede cargar resultado porque el partido ya esta FINALIZADO");
		}
		if (match.getStatus() != MatchStatus.EN_JUEGO) {
			throw new BusinessRuleException("Solo se pueden cargar resultados de partidos que estan EN_JUEGO");
		}

		// El backend calcula la tendencia real; nunca confia en un valor calculado por el cliente.
		ResultTrend resultTrend = calculateResultTrend(request.homeGoals(), request.awayGoals());

		match.setHomeGoals(request.homeGoals());
		match.setAwayGoals(request.awayGoals());
		match.setResultTrend(resultTrend);
		match.setStatus(MatchStatus.FINALIZADO);
		Match savedMatch = matchRepository.save(match);

		// Regla de negocio: al finalizar el partido se recalculan los puntos de todos sus pronosticos.
		predictionScoringService.scoreMatchPredictions(savedMatch);

		// Regla de negocio: la fecha no se edita manualmente; se recalcula desde el estado de sus partidos.
		matchDayService.refreshStatusByMatchDayId(savedMatch.getMatchDay().getId());

		return toResponse(savedMatch);
	}

	/**
	 * Elimina un partido si todavia no empezo.
	 *
	 * @param id identificador del partido a eliminar.
	 */
	@Transactional // El borrado y el recalculo de fecha forman una unica unidad de trabajo.
	public void deleteMatch(Long id) {
		Match match = getMatchEntityById(id);

		// Regla de negocio: no se elimina un partido que ya empezo porque afectaria pronosticos/resultados futuros.
		if (match.getStatus() != MatchStatus.POR_JUGARSE) {
			throw new BusinessRuleException("No se puede eliminar un partido que no esta POR_JUGARSE");
		}

		// Regla de negocio: eliminar un partido con pronosticos romperia la integridad del sistema.
		validateMatchHasNoPredictions(
				match.getId(),
				"No se puede eliminar el partido porque tiene pronosticos asociados"
		);

		Long matchDayId = match.getMatchDay().getId();
		matchRepository.delete(match);
		matchDayService.refreshStatusByMatchDayId(matchDayId);
	}

	/**
	 * Lista todos los partidos ordenados por horario ascendente.
	 *
	 * No recibe parametros.
	 * @return lista ordenada de partidos como DTOs.
	 */
	@Transactional(readOnly = true) // Consulta sin modificar la base.
	public List<MatchResponse> getAllMatches() {
		return matchRepository.findAllByOrderByStartTimeAsc()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Lista los partidos de una fecha ordenados por horario ascendente.
	 *
	 * @param matchDayId identificador de la fecha.
	 * @return lista de partidos de esa fecha.
	 */
	@Transactional(readOnly = true) // Consulta filtrada por fecha sin cambios en base.
	public List<MatchResponse> getMatchesByMatchDay(Long matchDayId) {
		getMatchDayEntityById(matchDayId);

		return matchRepository.findByMatchDayIdOrderByStartTimeAsc(matchDayId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Obtiene un partido por id.
	 *
	 * @param id identificador del partido.
	 * @return DTO del partido encontrado.
	 */
	@Transactional(readOnly = true) // Solo consulta un partido.
	public MatchResponse getMatchById(Long id) {
		return toResponse(getMatchEntityById(id));
	}

	/**
	 * Verifica que local y visitante sean equipos distintos.
	 *
	 * @param homeTeamId id del equipo local.
	 * @param awayTeamId id del equipo visitante.
	 */
	private void validateDifferentTeams(Long homeTeamId, Long awayTeamId) {
		// Regla de negocio: un equipo no puede jugar contra si mismo en el mismo partido.
		if (Objects.equals(homeTeamId, awayTeamId)) {
			throw new BusinessRuleException("El equipo local y el equipo visitante no pueden ser el mismo");
		}
	}

	/**
	 * Verifica que una fecha todavia permita agregar partidos.
	 *
	 * @param matchDay fecha donde se quiere crear el partido.
	 */
	private void validateMatchDayIsScheduled(MatchDay matchDay) {
		// Evita cambiar la estructura de una jornada ya iniciada y prepara pronosticos, resultados y ranking futuros.
		if (matchDay.getStatus() != MatchDayStatus.PROGRAMADA) {
			throw new BusinessRuleException("No se pueden crear partidos en una fecha que no esta en estado PROGRAMADA");
		}
	}

	/**
	 * Valida que el horario exista.
	 *
	 * @param startTime instante recibido desde el cliente.
	 * @return el mismo instante validado.
	 */
	private Instant validateStartTime(Instant startTime) {
		if (startTime == null) {
			throw new BusinessRuleException("El horario de inicio es obligatorio");
		}
		return startTime;
	}

	/**
	 * Calcula la tendencia real de un partido a partir de los goles cargados.
	 *
	 * Es la misma logica que usa el pronostico, pero aplicada al resultado real:
	 * sirve para comparar despues pronostico contra resultado y asignar puntos.
	 *
	 * @param homeGoals goles reales del local.
	 * @param awayGoals goles reales del visitante.
	 * @return LOCAL si gano el local, VISITANTE si gano el visitante o EMPATE si fueron iguales.
	 */
	private ResultTrend calculateResultTrend(int homeGoals, int awayGoals) {
		if (homeGoals > awayGoals) {
			return ResultTrend.LOCAL;
		}

		if (homeGoals < awayGoals) {
			return ResultTrend.VISITANTE;
		}

		return ResultTrend.EMPATE;
	}

	/**
	 * Verifica que un partido no tenga pronosticos asociados.
	 *
	 * Un partido con pronosticos no debe modificarse ni eliminarse porque los
	 * usuarios ya tomaron decisiones sobre equipos, horario y contexto del partido.
	 *
	 * @param matchId id del partido a validar.
	 * @param message mensaje de negocio que se devuelve si hay pronosticos.
	 */
	private void validateMatchHasNoPredictions(Long matchId, String message) {
		if (predictionRepository.existsByMatchId(matchId)) {
			throw new BusinessRuleException(message);
		}
	}

	/**
	 * Busca una fecha por id.
	 *
	 * @param id identificador de la fecha.
	 * @return entidad MatchDay encontrada.
	 */
	private MatchDay getMatchDayEntityById(Long id) {
		return matchDayRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada"));
	}

	/**
	 * Busca un equipo por id con mensaje especifico segun el rol del equipo en el partido.
	 *
	 * @param id identificador del equipo.
	 * @param message mensaje para responder si no existe.
	 * @return entidad Team encontrada.
	 */
	private Team getTeamEntityById(Long id, String message) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(message));
	}

	/**
	 * Busca un partido por id.
	 *
	 * @param id identificador del partido.
	 * @return entidad Match encontrada.
	 */
	private Match getMatchEntityById(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
	}

	/**
	 * Convierte entidad Match a DTO de salida.
	 *
	 * @param match entidad JPA interna.
	 * @return DTO con ids, nombres, horario, estado y datos de resultado si existieran.
	 */
	private MatchResponse toResponse(Match match) {
		return new MatchResponse(
				match.getId(),
				match.getMatchDay().getId(),
				match.getMatchDay().getName(),
				match.getHomeTeam().getId(),
				match.getHomeTeam().getName(),
				match.getAwayTeam().getId(),
				match.getAwayTeam().getName(),
				match.getStartTime(),
				match.getStatus(),
				match.getHomeGoals(),
				match.getAwayGoals(),
				match.getResultTrend(),
				match.getCreatedAt(),
				match.getUpdatedAt()
		);
	}
}
