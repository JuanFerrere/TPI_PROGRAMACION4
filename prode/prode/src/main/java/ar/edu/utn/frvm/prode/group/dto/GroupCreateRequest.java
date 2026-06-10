package ar.edu.utn.frvm.prode.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(

		@NotBlank(message = "El nombre del grupo es obligatorio")
		@Size(max = 100, message = "El nombre del grupo no puede superar 100 caracteres")
		String name

) {}
