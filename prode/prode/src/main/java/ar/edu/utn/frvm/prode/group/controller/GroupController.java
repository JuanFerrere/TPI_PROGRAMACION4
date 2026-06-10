package ar.edu.utn.frvm.prode.group.controller;

import ar.edu.utn.frvm.prode.group.dto.GroupCreateRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupDetailResponse;
import ar.edu.utn.frvm.prode.group.dto.GroupJoinRequest;
import ar.edu.utn.frvm.prode.group.dto.GroupResponse;
import ar.edu.utn.frvm.prode.group.service.GroupService;
import ar.edu.utn.frvm.prode.ranking.dto.RankingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para grupos privados.
 *
 * Expone endpoints bajo /api/groups y delega todas las reglas de negocio en
 * GroupService. Todos los endpoints requieren autenticación JWT válida.
 * La validación de pertenencia al grupo la hace el service, no el controller.
 */
@RestController // Indica que la clase recibe HTTP y responde JSON.
@RequestMapping("/api/groups") // Prefijo común para todos los endpoints de grupos.
public class GroupController {

	private final GroupService groupService;

	/**
	 * Constructor con inyección del service.
	 *
	 * @param groupService service con lógica de grupos privados.
	 */
	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	/**
	 * Crea un grupo privado nuevo.
	 *
	 * El usuario autenticado queda como owner y primer miembro del grupo.
	 * El backend genera el código de invitación automáticamente.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param request        body JSON con el nombre del grupo.
	 * @param authentication usuario autenticado desde Spring Security.
	 * @return grupo creado con su código de invitación.
	 */
	@PostMapping // Atiende POST /api/groups.
	@ResponseStatus(HttpStatus.CREATED) // Devuelve 201 Created al crear el grupo.
	@PreAuthorize("isAuthenticated()") // Cualquier usuario con JWT válido puede crear grupos.
	public GroupResponse createGroup(
			@Valid @RequestBody GroupCreateRequest request,
			Authentication authentication
	) {
		return groupService.createGroup(request, authentication);
	}

	/**
	 * Une al usuario autenticado a un grupo mediante código de invitación.
	 *
	 * Si el usuario ya pertenece al grupo o el código no existe, se devuelve error.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param request        body JSON con el código de invitación.
	 * @param authentication usuario autenticado desde Spring Security.
	 * @return grupo al que se unió el usuario.
	 */
	@PostMapping("/join") // Atiende POST /api/groups/join.
	@PreAuthorize("isAuthenticated()") // Cualquier usuario con JWT válido puede unirse a grupos.
	public GroupResponse joinGroup(
			@Valid @RequestBody GroupJoinRequest request,
			Authentication authentication
	) {
		return groupService.joinGroup(request, authentication);
	}

	/**
	 * Lista los grupos a los que pertenece el usuario autenticado.
	 *
	 * Si el usuario no pertenece a ningún grupo, devuelve lista vacía.
	 *
	 * Rol permitido: cualquier usuario autenticado.
	 *
	 * @param authentication usuario autenticado desde Spring Security.
	 * @return lista de grupos del usuario.
	 */
	@GetMapping("/me") // Atiende GET /api/groups/me.
	@PreAuthorize("isAuthenticated()") // Cada usuario ve solo sus propios grupos.
	public List<GroupResponse> getMyGroups(Authentication authentication) {
		return groupService.getMyGroups(authentication);
	}

	/**
	 * Devuelve el detalle completo de un grupo, incluyendo la lista de miembros.
	 *
	 * Solo pueden acceder usuarios que ya pertenecen al grupo. El service
	 * valida la pertenencia y devuelve error si el usuario no es miembro.
	 *
	 * Rol permitido: usuario autenticado y miembro del grupo.
	 *
	 * @param groupId        id del grupo.
	 * @param authentication usuario autenticado desde Spring Security.
	 * @return detalle del grupo con lista de miembros.
	 */
	@GetMapping("/{groupId}") // Atiende GET /api/groups/{groupId}.
	@PreAuthorize("isAuthenticated()") // La validación de pertenencia la hace el service.
	public GroupDetailResponse getGroupDetail(
			@PathVariable Long groupId,
			Authentication authentication
	) {
		return groupService.getGroupDetail(groupId, authentication);
	}

	/**
	 * Elimina la membresía del usuario autenticado en un grupo.
	 *
	 * El owner no puede abandonar su propio grupo. Los no miembros reciben error.
	 *
	 * Rol permitido: usuario autenticado y miembro del grupo.
	 *
	 * @param groupId        id del grupo.
	 * @param authentication usuario autenticado desde Spring Security.
	 */
	@DeleteMapping("/{groupId}/members/me") // Atiende DELETE /api/groups/{groupId}/members/me.
	@ResponseStatus(HttpStatus.NO_CONTENT) // Devuelve 204 al salir correctamente.
	@PreAuthorize("isAuthenticated()") // La validación de pertenencia y la regla del owner la hace el service.
	public void leaveGroup(
			@PathVariable Long groupId,
			Authentication authentication
	) {
		groupService.leaveGroup(groupId, authentication);
	}

	/**
	 * Devuelve el ranking de un grupo ordenado por puntos.
	 *
	 * Solo incluye usuarios que son miembros del grupo. Los puntos provienen
	 * de los pronósticos calculados en Etapa 5, no se recalculan aquí.
	 * Todos los miembros aparecen aunque no tengan pronósticos (con 0 puntos).
	 *
	 * Rol permitido: usuario autenticado y miembro del grupo.
	 *
	 * @param groupId        id del grupo.
	 * @param authentication usuario autenticado desde Spring Security.
	 * @return lista ordenada de posiciones del ranking del grupo.
	 */
	@GetMapping("/{groupId}/ranking") // Atiende GET /api/groups/{groupId}/ranking.
	@PreAuthorize("isAuthenticated()") // La validación de pertenencia la hace el service.
	public List<RankingResponse> getGroupRanking(
			@PathVariable Long groupId,
			Authentication authentication
	) {
		return groupService.getGroupRanking(groupId, authentication);
	}
}
