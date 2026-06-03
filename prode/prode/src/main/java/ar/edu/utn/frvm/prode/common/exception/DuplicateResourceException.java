package ar.edu.utn.frvm.prode.common.exception;

/**
 * Excepcion para indicar que se intento crear un dato duplicado.
 *
 * Ejemplo: registrar un username o email que ya existe.
 */
public class DuplicateResourceException extends RuntimeException {

	/**
	 * Crea una excepcion con un mensaje explicativo.
	 *
	 * @param message mensaje que explica que dato ya existe.
	 */
	public DuplicateResourceException(String message) {
		super(message);
	}
}
