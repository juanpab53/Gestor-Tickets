package service.exceptions;

/**
 * Excepción personalizada para errores de lógica de negocio relacionados con
 * respuestas.
 */
public class RespuestaException extends Exception {
	private static final long serialVersionUID = 1L;

	public RespuestaException(String message) {
		super(message);
	}
}
