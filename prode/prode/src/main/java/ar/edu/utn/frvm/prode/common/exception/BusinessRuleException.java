package ar.edu.utn.frvm.prode.common.exception;

/**
 * Excepcion para reglas de negocio simples.
 *
 * Se usa cuando la peticion tiene datos validos en formato,
 * pero no cumple una regla del sistema.
 */
public class BusinessRuleException extends RuntimeException {

	/**
	 * Crea una excepcion con un mensaje explicativo.
	 *
	 * @param message mensaje que explica la regla incumplida.
	 */
	public BusinessRuleException(String message) {
		super(message);
	}
}
