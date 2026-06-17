package ar.edu.utn.frvm.prode.tournament.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.DuplicateResourceException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentCreateRequest;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentResponse;
import ar.edu.utn.frvm.prode.tournament.dto.TournamentStatusUpdateRequest;
import ar.edu.utn.frvm.prode.tournament.entity.Tournament;
import ar.edu.utn.frvm.prode.tournament.entity.TournamentStatus;
import ar.edu.utn.frvm.prode.tournament.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de torneos.
 *
 * Contiene la logica minima de Tournament sin relacionarlo todavia con otros
 * modulos del Prode.
 */
@Service
public class TournamentService {

	private static final int MAX_NAME_LENGTH = 120;
	private static final int MAX_DESCRIPTION_LENGTH = 500;

	private final TournamentRepository tournamentRepository;

	/**
	 * Constructor con repositorio de torneos.
	 *
	 * @param tournamentRepository repositorio para consultar y guardar torneos.
	 */
	public TournamentService(TournamentRepository tournamentRepository) {
		this.tournamentRepository = tournamentRepository;
	}

	/**
	 * Crea un torneo nuevo con estado inicial DRAFT.
	 *
	 * @param request DTO de entrada con nombre y descripcion opcional.
	 * @return torneo creado como DTO de salida.
	 */
	@Transactional
	public TournamentResponse create(TournamentCreateRequest request) {
		String name = normalizeName(request.name());
		String description = normalizeDescription(request.description());

		if (tournamentRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Ya existe un torneo con ese nombre");
		}

		Tournament tournament = new Tournament(name, description);
		tournament.setStatus(TournamentStatus.DRAFT);
		Tournament savedTournament = tournamentRepository.save(tournament);
		return toResponse(savedTournament);
	}

	/**
	 * Lista todos los torneos ordenados por creacion descendente.
	 *
	 * @return lista de torneos como DTOs.
	 */
	@Transactional(readOnly = true)
	public List<TournamentResponse> findAll() {
		return tournamentRepository.findAllByOrderByCreatedAtDesc()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Obtiene un torneo por id.
	 *
	 * @param tournamentId identificador del torneo.
	 * @return torneo encontrado como DTO.
	 */
	@Transactional(readOnly = true)
	public TournamentResponse findById(Long tournamentId) {
		return toResponse(getTournamentEntityById(tournamentId));
	}

	/**
	 * Actualiza el estado de un torneo.
	 *
	 * @param tournamentId identificador del torneo.
	 * @param request DTO con el nuevo estado.
	 * @return torneo actualizado como DTO.
	 */
	@Transactional
	public TournamentResponse updateStatus(Long tournamentId, TournamentStatusUpdateRequest request) {
		Tournament tournament = getTournamentEntityById(tournamentId);
		TournamentStatus status = parseStatus(request.status());

		tournament.setStatus(status);
		return toResponse(tournament);
	}

	private Tournament getTournamentEntityById(Long tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessRuleException("El nombre del torneo es obligatorio");
		}

		String normalizedName = name.trim();

		if (normalizedName.length() > MAX_NAME_LENGTH) {
			throw new BusinessRuleException("El nombre del torneo no puede superar 120 caracteres");
		}

		return normalizedName;
	}

	private String normalizeDescription(String description) {
		if (description == null) {
			return null;
		}

		String normalizedDescription = description.trim();

		if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
			throw new BusinessRuleException("La descripcion del torneo no puede superar 500 caracteres");
		}

		return normalizedDescription.isEmpty() ? null : normalizedDescription;
	}

	private TournamentStatus parseStatus(String status) {
		if (status == null || status.isBlank()) {
			throw new BusinessRuleException("El estado del torneo es obligatorio");
		}

		try {
			return TournamentStatus.valueOf(status.trim());
		} catch (IllegalArgumentException exception) {
			throw new BusinessRuleException("Estado de torneo invalido");
		}
	}

	private TournamentResponse toResponse(Tournament tournament) {
		return new TournamentResponse(
				tournament.getId(),
				tournament.getName(),
				tournament.getDescription(),
				tournament.getStatus(),
				tournament.getCreatedAt(),
				tournament.getUpdatedAt()
		);
	}
}
