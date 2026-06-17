package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.match.repository.MatchRepository;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import ar.edu.utn.frvm.prode.matchday.repository.MatchDayRepository;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayBulkCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentMatchDayResponse;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Service para administrar fechas dentro de un torneo.
 */
@Service
public class TournamentMatchDayService {

	private static final int MAX_NAME_LENGTH = 100;

	private final TournamentRepository tournamentRepository;
	private final MatchDayRepository matchDayRepository;
	private final MatchRepository matchRepository;

	public TournamentMatchDayService(
			TournamentRepository tournamentRepository,
			MatchDayRepository matchDayRepository,
			MatchRepository matchRepository
	) {
		this.tournamentRepository = tournamentRepository;
		this.matchDayRepository = matchDayRepository;
		this.matchRepository = matchRepository;
	}

	@Transactional(readOnly = true)
	public List<TournamentMatchDayResponse> findAll(Long tournamentId) {
		getTournamentById(tournamentId);

		return matchDayRepository.findByTournamentIdOrderByOrderNumberAscIdAsc(tournamentId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public TournamentMatchDayResponse create(Long tournamentId, TournamentMatchDayCreateRequest request) {
		Tournament tournament = getTournamentById(tournamentId);
		return toResponse(createInternal(tournament, request, null));
	}

	@Transactional
	public List<TournamentMatchDayResponse> createBulk(
			Long tournamentId,
			TournamentMatchDayBulkCreateRequest request
	) {
		Tournament tournament = getTournamentById(tournamentId);
		Set<String> namesInRequest = new HashSet<>();
		Set<Integer> ordersInRequest = new HashSet<>();

		for (int index = 0; index < request.matchDays().size(); index++) {
			TournamentMatchDayCreateRequest item = request.matchDays().get(index);
			int lineNumber = index + 1;
			String normalizedName = normalizeName(item.name(), lineNumber);
			Integer orderNumber = normalizeOrderNumber(item.orderNumber(), lineNumber);

			if (!namesInRequest.add(normalizedName.toLowerCase(Locale.ROOT))) {
				throw new DuplicateResourceException("Item " + lineNumber + ": hay una fecha repetida en la carga");
			}

			if (orderNumber != null && !ordersInRequest.add(orderNumber)) {
				throw new DuplicateResourceException("Item " + lineNumber + ": hay un numero de orden repetido en la carga");
			}
		}

		return request.matchDays()
				.stream()
				.map(item -> createInternal(tournament, item, null))
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void remove(Long tournamentId, Long matchDayId) {
		getTournamentById(tournamentId);
		MatchDay matchDay = getMatchDayById(tournamentId, matchDayId);

		if (matchRepository.existsByMatchDayId(matchDayId)) {
			throw new BusinessRuleException("No se puede eliminar una fecha que tiene partidos asociados");
		}

		matchDayRepository.delete(matchDay);
	}

	private MatchDay createInternal(
			Tournament tournament,
			TournamentMatchDayCreateRequest request,
			Integer itemNumber
	) {
		String name = normalizeName(request.name(), itemNumber);
		Integer orderNumber = normalizeOrderNumber(request.orderNumber(), itemNumber);

		if (matchDayRepository.existsByTournamentIdAndNameIgnoreCase(tournament.getId(), name)) {
			throw new DuplicateResourceException(buildItemMessage("Ya existe una fecha con ese nombre en este torneo", itemNumber));
		}

		if (orderNumber != null && matchDayRepository.existsByTournamentIdAndOrderNumber(tournament.getId(), orderNumber)) {
			throw new DuplicateResourceException(buildItemMessage("Ya existe una fecha con ese numero de orden en este torneo", itemNumber));
		}

		MatchDay matchDay = new MatchDay(name, tournament, orderNumber);
		matchDay.setStatus(MatchDayStatus.PROGRAMADA);
		return matchDayRepository.save(matchDay);
	}

	private Tournament getTournamentById(Long tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
	}

	private MatchDay getMatchDayById(Long tournamentId, Long matchDayId) {
		return matchDayRepository.findByIdAndTournamentId(matchDayId, tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Fecha del torneo no encontrada"));
	}

	private String normalizeName(String name, Integer itemNumber) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException(buildItemMessage("El nombre de la fecha es obligatorio", itemNumber));
		}

		String normalizedName = name.trim();

		if (normalizedName.length() > MAX_NAME_LENGTH) {
			throw new BusinessRuleException(buildItemMessage("El nombre de la fecha no puede superar 100 caracteres", itemNumber));
		}

		return normalizedName;
	}

	private Integer normalizeOrderNumber(Integer orderNumber, Integer itemNumber) {
		if (orderNumber != null && orderNumber <= 0) {
			throw new BusinessRuleException(buildItemMessage("El numero de orden debe ser positivo", itemNumber));
		}

		return orderNumber;
	}

	private String buildItemMessage(String message, Integer itemNumber) {
		if (itemNumber == null) {
			return message;
		}

		return "Item " + itemNumber + ": " + message;
	}

	private TournamentMatchDayResponse toResponse(MatchDay matchDay) {
		return new TournamentMatchDayResponse(
				matchDay.getId(),
				matchDay.getTournament().getId(),
				matchDay.getName(),
				matchDay.getOrderNumber(),
				matchDay.getCreatedAt(),
				matchDay.getUpdatedAt()
		);
	}
}
