package ar.edu.utn.frvm.prode.common.exception;

/**
 * Excepcion para indicar que un recurso solicitado no existe.
 *
 * En esta etapa puede usarse para usuarios no encontrados.
 */
public class ResourceNotFoundException extends RuntimeException {

	/**
	 * Crea una excepcion con un mensaje explicativo.
	 *
	 * @param message mensaje que explica que recurso no se encontro.
	 */
	public ResourceNotFoundException(String message) {
		super(message);
	}
}
