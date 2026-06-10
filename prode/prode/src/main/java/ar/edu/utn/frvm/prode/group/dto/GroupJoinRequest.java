package ar.edu.utn.frvm.prode.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para unirse a un grupo privado.
 *
 * El cliente envía el código de invitación que obtuvo del dueño del grupo.
 * El backend busca el grupo por ese código y agrega al usuario autenticado
 * como miembro si no pertenece ya.
 */
public record GroupJoinRequest(

		@NotBlank(message = "El codigo de invitacion es obligatorio")
		@Size(max = 50, message = "El codigo de invitacion no puede superar 50 caracteres")
		String inviteCode

) {}
