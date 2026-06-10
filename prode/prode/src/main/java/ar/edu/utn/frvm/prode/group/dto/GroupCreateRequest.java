package ar.edu.utn.frvm.prode.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear un grupo privado.
 *
 * El cliente solo envía el nombre. El backend genera el código de invitación,
 * asigna el owner y fija la fecha de creación.
 */
public record GroupCreateRequest(

		@NotBlank(message = "El nombre del grupo es obligatorio")
		@Size(max = 100, message = "El nombre del grupo no puede superar 100 caracteres")
		String name

) {}
