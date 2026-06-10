package ar.edu.utn.frvm.prode.group.service;

import ar.edu.utn.frvm.prode.common.exception.BusinessRuleException;
import ar.edu.utn.frvm.prode.common.exception.ResourceNotFoundException;
import ar.edu.utn.frvm.prode.group.dto.GroupCreateRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupDetailResponse;
import ar.edu.utn.frvm.prode.group.dto.GroupJoinRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupMemberResponse;
import ar.edu.utn.frvm.prode.group.dto.GroupResponse;
import ar.edu.utn.frvm.prode.group.entity.GroupMember;
import ar.edu.utn.frvm.prode.group.entity.PrivateGroup;
import ar.edu.utn.frvm.prode.group.repository.GroupMemberRepository;
import ar.edu.utn.frvm.prode.group.repository.PrivateGroupRepository;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import ar.edu.utn.frvm.prode.prediction.repository.PredictionRepository;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import ar.edu.utn.frvm.prode.user.entity.User;
import ar.edu.utn.frvm.prode.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service de grupos privados.
 *
 * Centraliza las reglas de negocio de Etapa 6: creacion, ingreso, detalle,
 * salida y ranking por grupo. El ranking reutiliza la lógica de acumulacion de
 * puntos del ranking global, pero filtrando únicamente los miembros del grupo.
 *
 * Estrategia del codigo de invitacion:
 * Se toman los primeros 8 caracteres del UUID generado (sin guiones) en
 * mayusculas. Esto produce un codigo hexadecimal en mayusculas como "3F2504E0".
 * La longitud de 8 caracteres sobre un alfabeto de 16 posibles da 16^8 = ~4.3
 * mil millones de combinaciones. Para el tamaño de este TPI es más que suficiente.
 * Se verifica unicidad en base antes de guardar con un bucle do-while.
 */
@Service // Spring registra esta clase como service de dominio.
public class GroupService {

	private final PrivateGroupRepository privateGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final PredictionRepository predictionRepository;
	private final UserRepository userRepository;

	/**
	 * Constructor con repositorios necesarios.
	 *
	 * @param privateGroupRepository repositorio para grupos privados.
	 * @param groupMemberRepository  repositorio para membresías de grupos.
	 * @param predictionRepository   repositorio para calcular el ranking del grupo.
	 * @param userRepository         repositorio para resolver el usuario autenticado.
	 */
	public GroupService(
			PrivateGroupRepository privateGroupRepository,
			GroupMemberRepository groupMemberRepository,
			PredictionRepository predictionRepository,
			UserRepository userRepository
	) {
		this.privateGroupRepository = privateGroupRepository;
		this.groupMemberRepository = groupMemberRepository;
		this.predictionRepository = predictionRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Crea un grupo privado y agrega al creador como primer miembro.
	 *
	 * El backend genera el código de invitación: el cliente solo elige el nombre.
	 * El owner queda como miembro automáticamente al crear el grupo.
	 *
	 * @param request        nombre elegido por el usuario.
	 * @param authentication usuario autenticado.
	 * @return grupo creado como DTO.
	 */
	@Transactional // Creación del grupo y del miembro owner forman una sola unidad de trabajo.
	public GroupResponse createGroup(GroupCreateRequest request, Authentication authentication) {
		User owner = getAuthenticatedUser(authentication);

		String inviteCode = generateInviteCode();

		PrivateGroup group = new PrivateGroup(request.name(), inviteCode, owner);
		PrivateGroup savedGroup = privateGroupRepository.save(group);

		// El owner queda como primer miembro del grupo automáticamente.
		GroupMember ownerMember = new GroupMember(savedGroup, owner);
		groupMemberRepository.save(ownerMember);

		long membersCount = groupMemberRepository.countByGroupId(savedGroup.getId());
		return toGroupResponse(savedGroup, (int) membersCount);
	}

	/**
	 * Une al usuario autenticado a un grupo usando el código de invitación.
	 *
	 * Si el usuario ya pertenece al grupo, se devuelve un error claro.
	 * Si el código no existe, también se devuelve un error claro.
	 *
	 * @param request        código de invitación enviado por el cliente.
	 * @param authentication usuario autenticado.
	 * @return grupo al que se unió como DTO.
	 */
	@Transactional // Validaciones y creación de membresía en una sola unidad de trabajo.
	public GroupResponse joinGroup(GroupJoinRequest request, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = privateGroupRepository.findByInviteCode(request.inviteCode())
				.orElseThrow(() -> new ResourceNotFoundException(
						"No existe un grupo con el codigo de invitacion indicado"
				));

		// Regla de negocio: no se permite pertenecer dos veces al mismo grupo.
		if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
			throw new BusinessRuleException("El usuario ya pertenece a este grupo");
		}

		GroupMember member = new GroupMember(group, user);
		groupMemberRepository.save(member);

		long membersCount = groupMemberRepository.countByGroupId(group.getId());
		return toGroupResponse(group, (int) membersCount);
	}

	/**
	 * Lista los grupos a los que pertenece el usuario autenticado.
	 *
	 * Si el usuario no pertenece a ningún grupo, devuelve lista vacía (no error).
	 *
	 * @param authentication usuario autenticado.
	 * @return lista de grupos del usuario.
	 */
	@Transactional(readOnly = true) // Solo consulta, no modifica nada en la base.
	public List<GroupResponse> getMyGroups(Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		List<GroupMember> memberships = groupMemberRepository.findByUserId(user.getId());

		return memberships.stream()
				.map(membership -> {
					PrivateGroup group = membership.getGroup();
					long membersCount = groupMemberRepository.countByGroupId(group.getId());
					return toGroupResponse(group, (int) membersCount);
				})
				.toList();
	}

	/**
	 * Devuelve el detalle completo de un grupo, incluyendo la lista de miembros.
	 *
	 * Solo pueden acceder los usuarios que ya pertenecen al grupo.
	 *
	 * @param groupId        id del grupo.
	 * @param authentication usuario autenticado.
	 * @return detalle del grupo con lista de miembros.
	 */
	@Transactional(readOnly = true) // Solo consulta, no modifica nada en la base.
	public GroupDetailResponse getGroupDetail(Long groupId, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = getGroupEntityById(groupId);

		// Regla de seguridad: solo miembros pueden ver el detalle del grupo.
		validateIsMember(group.getId(), user.getId(), "No tenes permisos para acceder a este grupo");

		List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());

		List<GroupMemberResponse> memberResponses = members.stream()
				.map(m -> new GroupMemberResponse(
						m.getUser().getId(),
						m.getUser().getUsername(),
						m.getJoinedAt()
				))
				.toList();

		return new GroupDetailResponse(
				group.getId(),
				group.getName(),
				group.getInviteCode(),
				group.getOwner().getId(),
				group.getOwner().getUsername(),
				members.size(),
				memberResponses,
				group.getCreatedAt()
		);
	}

	/**
	 * Elimina la membresía del usuario autenticado en un grupo.
	 *
	 * Regla: el owner no puede abandonar su propio grupo. Esta restricción
	 * simplifica el TPI evitando implementar transferencia de propiedad o
	 * eliminación en cascada del grupo cuando queda sin dueño.
	 *
	 * @param groupId        id del grupo.
	 * @param authentication usuario autenticado.
	 */
	@Transactional // Validaciones y eliminación de membresía en una sola unidad de trabajo.
	public void leaveGroup(Long groupId, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = getGroupEntityById(groupId);

		// Regla de negocio: solo miembros pueden salir del grupo.
		validateIsMember(group.getId(), user.getId(), "No perteneces a este grupo");

		// Regla de negocio: el owner no puede abandonar el grupo (simplificacion del TPI).
		if (group.getOwner().getId().equals(user.getId())) {
			throw new BusinessRuleException("El creador del grupo no puede abandonar el grupo");
		}

		groupMemberRepository.deleteByGroupIdAndUserId(group.getId(), user.getId());
	}

	/**
	 * Calcula el ranking de un grupo filtrando solo sus miembros.
	 *
	 * Los puntos provienen de los pronósticos ya calculados en Etapa 5.
	 * No se recalculan puntos: se suman los que ya tiene cada pronóstico.
	 * El orden es: más puntos primero, desempate por exactHits, luego username.
	 *
	 * Diferencia clave con el ranking global: todos los miembros aparecen en
	 * el ranking aunque no tengan pronósticos (con 0 puntos). Esto hace más
	 * claro para el grupo quién participó y quién no.
	 *
	 * @param groupId        id del grupo.
	 * @param authentication usuario autenticado.
	 * @return lista ordenada de posiciones del ranking del grupo.
	 */
	@Transactional(readOnly = true) // Solo consulta, no modifica nada en la base.
	public List<RankingResponse> getGroupRanking(Long groupId, Authentication authentication) {
		User user = getAuthenticatedUser(authentication);

		PrivateGroup group = getGroupEntityById(groupId);

		// Regla de seguridad: solo miembros pueden ver el ranking del grupo.
		validateIsMember(group.getId(), user.getId(), "No tenes permisos para ver el ranking de este grupo");

		List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
		List<Long> memberUserIds = members.stream()
				.map(m -> m.getUser().getId())
				.toList();

		// Se pre-inicializan acumuladores para todos los miembros con 0 puntos.
		// Así aparecen en el ranking aunque no tengan pronósticos cargados.
		Map<Long, UserScoreAccumulator> accumulatorByUser = new LinkedHashMap<>();
		for (GroupMember member : members) {
			User memberUser = member.getUser();
			accumulatorByUser.put(memberUser.getId(),
					new UserScoreAccumulator(memberUser.getId(), memberUser.getUsername()));
		}

		// Se suman puntos de los pronósticos solo de los miembros del grupo.
		List<Prediction> predictions = memberUserIds.isEmpty()
				? List.of()
				: predictionRepository.findByUserIdIn(memberUserIds);

		for (Prediction prediction : predictions) {
			UserScoreAccumulator accumulator = accumulatorByUser.get(prediction.getUser().getId());
			if (accumulator != null) {
				accumulator.add(prediction);
			}
		}

		// Mismo criterio de orden que el ranking global.
		Comparator<UserScoreAccumulator> rankingOrder = Comparator
				.comparingInt(UserScoreAccumulator::getTotalPoints).reversed()         // 1) más puntos primero.
				.thenComparing(Comparator.comparingLong(UserScoreAccumulator::getExactHits).reversed()) // 2) más aciertos exactos.
				.thenComparing(UserScoreAccumulator::getUsername);                     // 3) username alfabético como desempate estable.

		List<UserScoreAccumulator> ordered = accumulatorByUser.values().stream()
				.sorted(rankingOrder)
				.toList();

		List<RankingResponse> ranking = new ArrayList<>();
		int position = 1;
		for (UserScoreAccumulator accumulator : ordered) {
			ranking.add(new RankingResponse(
					position,
					accumulator.getUserId(),
					accumulator.getUsername(),
					accumulator.getTotalPoints(),
					accumulator.getExactHits(),
					accumulator.getPredictionsCount()
			));
			position++;
		}

		return ranking;
	}

	/**
	 * Verifica que el usuario sea miembro del grupo.
	 *
	 * @param groupId id del grupo.
	 * @param userId  id del usuario.
	 * @param message mensaje de error si no pertenece.
	 */
	private void validateIsMember(Long groupId, Long userId, String message) {
		if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
			throw new BusinessRuleException(message);
		}
	}

	/**
	 * Busca un grupo por id.
	 *
	 * @param id id del grupo.
	 * @return entidad PrivateGroup encontrada.
	 */
	private PrivateGroup getGroupEntityById(Long id) {
		return privateGroupRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
	}

	/**
	 * Genera un código de invitación único de 8 caracteres hexadecimales en mayúsculas.
	 *
	 * Se usa UUID como fuente de aleatoriedad. Se repite hasta obtener un código
	 * que no exista en la base. En la práctica, la colisión es extremadamente
	 * improbable para el volumen de este TPI.
	 *
	 * @return código de invitación único, por ejemplo "3F2504E0".
	 */
	private String generateInviteCode() {
		String code;
		do {
			code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
		} while (privateGroupRepository.existsByInviteCode(code));
		return code;
	}

	/**
	 * Obtiene el usuario autenticado a partir de Spring Security.
	 *
	 * El filtro JWT deja cargado el username en Authentication. Con ese username
	 * se consulta la base para obtener la entidad User real.
	 *
	 * @param authentication datos de autenticación de la request.
	 * @return usuario autenticado.
	 */
	private User getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Usuario no autenticado");
		}

		String username = authentication.getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
	}

	/**
	 * Convierte entidad PrivateGroup a DTO de salida resumido.
	 *
	 * @param group       entidad JPA interna.
	 * @param membersCount cantidad de miembros calculada por el repositorio.
	 * @return DTO de salida.
	 */
	private GroupResponse toGroupResponse(PrivateGroup group, int membersCount) {
		return new GroupResponse(
				group.getId(),
				group.getName(),
				group.getInviteCode(),
				group.getOwner().getId(),
				group.getOwner().getUsername(),
				membersCount,
				group.getCreatedAt()
		);
	}

	/**
	 * Acumulador interno para sumar estadísticas de un miembro del grupo.
	 *
	 * Clase auxiliar privada idéntica en concepto a la de RankingService, pero
	 * acotada al ranking de grupo. Se duplica intencionalmente para no generar
	 * dependencias artificiales entre módulos.
	 */
	private static final class UserScoreAccumulator {

		private final Long userId;
		private final String username;
		private int totalPoints;
		private long exactHits;
		private long predictionsCount;

		private UserScoreAccumulator(Long userId, String username) {
			this.userId = userId;
			this.username = username;
		}

		private void add(Prediction prediction) {
			this.totalPoints += prediction.getPoints();
			if (Boolean.TRUE.equals(prediction.getExactHit())) {
				this.exactHits++;
			}
			this.predictionsCount++;
		}

		private Long getUserId() { return userId; }
		private String getUsername() { return username; }
		private int getTotalPoints() { return totalPoints; }
		private long getExactHits() { return exactHits; }
		private long getPredictionsCount() { return predictionsCount; }
	}
}
