package service.exceptions;

/**
 * Excepción personalizada para errores de lógica de negocio relacionados con
 * tickets.
 */
public class TicketException extends Exception {

	private static final long serialVersionUID = 1L;

	public TicketException(String message) {
		super(message);
	}
}
