package service.exceptions;

/**
 * Excepción personalizada para errores de lógica de negocio relacionados con
 * usuarios. Se utiliza para comunicar errores desde la capa de servicio a la
 * capa de presentación.
 */
public class UserException extends Exception {

	private static final long serialVersionUID = 1L;

	public UserException(String message) {
		super(message);
	}
}
