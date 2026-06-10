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

@Service
public class MatchDayService {
	private final MatchDayRepository matchDayRepository;
	private final MatchRepository matchRepository;

	public MatchDayService(MatchDayRepository matchDayRepository, MatchRepository matchRepository) {
		this.matchDayRepository = matchDayRepository;
		this.matchRepository = matchRepository;
	}

	@Transactional
	public MatchDayResponse createMatchDay(MatchDayCreateRequest request) {
		String name = normalizeName(request.name());

		if (matchDayRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe una fecha con ese nombre");
		}

		MatchDay matchDay = new MatchDay(name);
		matchDay.setStatus(MatchDayStatus.PROGRAMADA);
		MatchDay savedMatchDay = matchDayRepository.save(matchDay);
		return toResponse(savedMatchDay);
	}

	@Transactional
	public MatchDayResponse updateMatchDay(Long id, MatchDayUpdateRequest request) {
		MatchDay matchDay = getMatchDayEntityById(id);
		String name = normalizeName(request.name());

		if (matchDay.getStatus() != MatchDayStatus.PROGRAMADA) {
			throw new BusinessRuleException("No se puede modificar una fecha que no esta PROGRAMADA");
		}

		if (matchRepository.existsByMatchDayId(id)) {
			throw new BusinessRuleException("No se puede modificar una fecha que ya tiene partidos asociados");
		}

		if (!matchDay.getName().equalsIgnoreCase(name) && matchDayRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe una fecha con ese nombre");
		}

		matchDay.setName(name);
		return toResponse(matchDay);
	}

	@Transactional
	public void deleteMatchDay(Long id) {
		MatchDay matchDay = getMatchDayEntityById(id);

		if (matchDay.getStatus() != MatchDayStatus.PROGRAMADA) {
			throw new BusinessRuleException("No se puede eliminar una fecha que no esta PROGRAMADA");
		}

		if (matchRepository.existsByMatchDayId(id)) {
			throw new BusinessRuleException("No se puede eliminar una fecha que tiene partidos asociados");
		}

		matchDayRepository.delete(matchDay);
	}

	@Transactional(readOnly = true)
	public List<MatchDayResponse> getAllMatchDays() {
		return matchDayRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MatchDayResponse> getMatchDaysByStatus(MatchDayStatus status) {
		return matchDayRepository.findByStatus(status)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public MatchDayResponse getMatchDayById(Long id) {
		return toResponse(getMatchDayEntityById(id));
	}

	@Transactional
	public MatchDayResponse refreshStatusByMatchDayId(Long matchDayId) {
		MatchDay matchDay = getMatchDayEntityById(matchDayId);
		List<Match> matches = matchRepository.findByMatchDayIdOrderByStartTimeAsc(matchDayId);

		MatchDayStatus newStatus = MatchDayStatus.PROGRAMADA;

		if (!matches.isEmpty()) {
			boolean allFinished = matches.stream().allMatch(match -> match.getStatus() == MatchStatus.FINALIZADO);
			boolean anyInGame = matches.stream().anyMatch(match -> match.getStatus() == MatchStatus.EN_JUEGO);
			boolean allScheduled = matches.stream().allMatch(match -> match.getStatus() == MatchStatus.POR_JUGARSE);

			if (allFinished) {
				newStatus = MatchDayStatus.FINALIZADA;
			} else if (anyInGame) {
				newStatus = MatchDayStatus.EN_JUEGO;
			} else if (allScheduled) {
				newStatus = MatchDayStatus.PROGRAMADA;
			} else {
				newStatus = MatchDayStatus.EN_JUEGO;
			}
		}

		matchDay.setStatus(newStatus);
		return toResponse(matchDay);
	}

	private MatchDay getMatchDayEntityById(Long id) {
		return matchDayRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada"));
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException("El nombre de la fecha es obligatorio");
		}
		return name.trim();
	}

	private MatchDayResponse toResponse(MatchDay matchDay) {
		return new MatchDayResponse(
				matchDay.getId(),
				matchDay.getName(),
				matchDay.getStatus(),
				matchDay.getCreatedAt()
		);
	}
}
