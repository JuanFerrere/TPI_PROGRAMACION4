package ar.edu.utn.frvm.prode.user.entity;

/**
 * Enum que representa los roles disponibles dentro del sistema.
 *
 * Un enum se usa cuando los valores posibles son fijos y controlados.
 * En este caso, un usuario solo puede tener rol USER o ADMIN.
 */
public enum Role {
	/**
	 * Usuario comun del sistema. Podra cargar pronosticos en etapas futuras.
	 */
	USER,

	/**
	 * Usuario administrador. Podra gestionar datos del sistema en etapas futuras.
	 */
	ADMIN
}
