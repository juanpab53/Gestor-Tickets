package service;

import controller.RespuestaDAO;
import controller.TicketDAO;
import model.Respuesta;
import model.Ticket;
import model.Usuario;
import service.exceptions.*;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Capa de servicio para la gestión de respuestas en los tickets. Contiene la
 * lógica de negocio para añadir y consultar respuestas.
 */
public class GestorRespuesta {

	private final RespuestaDAO respuestaDAO;
	private final TicketDAO ticketDAO;

	/**
	 * Constructor que inicializa los DAOs necesarios.
	 */
	public GestorRespuesta() {
		this.respuestaDAO = new RespuestaDAO();
		this.ticketDAO = new TicketDAO();
	}

	/**
	 * Añade una nueva respuesta a un ticket existente.
	 *
	 * @param autor     El usuario que escribe la respuesta (cliente, técnico o
	 *                  admin).
	 * @param ticketId  El ID del ticket al que se responde.
	 * @param contenido El texto de la respuesta.
	 * @return La respuesta recién creada.
	 * @throws TicketException    si el ticket no se encuentra o está cerrado.
	 * @throws RespuestaException si ocurre un error al guardar la respuesta.
	 * @throws UsuarioException   si el autor no es válido.
	 */
	public Respuesta anadirRespuesta(Usuario autor, String ticketId, String contenido)
			throws TicketException, UserException, RespuestaException {
		if (autor == null) {
			throw new UserException("El autor de la respuesta no puede ser nulo.");
		}

		Ticket ticket = ticketDAO.findById(ticketId)
				.orElseThrow(() -> new TicketException("No se encontró el ticket con ID: " + ticketId));

		if (ticket.cerrado()) {
			throw new TicketException("No se puede responder a un ticket que ya está cerrado.");
		}

		Respuesta nuevaRespuesta = new Respuesta();
		nuevaRespuesta.setTicketId(ticketId);
		nuevaRespuesta.setAutorId(autor.getId());
		nuevaRespuesta.setContenido(contenido);
		nuevaRespuesta.setFecha(LocalDateTime.now());

		try {
			// Intentamos crear la respuesta. El DAO puede lanzar IOException si falla la
			// escritura.
			return respuestaDAO.crear(nuevaRespuesta)
					.orElseThrow(() -> new RespuestaException("Error interno: No se pudo guardar la respuesta."));
		} catch (IOException e) {
			// Si ocurre un error de I/O, lo envolvemos en una RespuestaException para
			// notificar a la capa superior.
			throw new RespuestaException("Error de persistencia al guardar la respuesta: " + e.getMessage());
		}
	}

	/**
	 * Consulta todas las respuestas de un ticket, ordenadas por fecha de creación.
	 *
	 * @param ticketId El ID del ticket.
	 * @return Una lista de respuestas ordenadas cronológicamente.
	 */
	public List<Respuesta> consultarRespuestasPorTicket(String ticketId) {
		return respuestaDAO.findByTicketId(ticketId).stream().sorted(Comparator.comparing(Respuesta::getFecha))
				.collect(Collectors.toList());
	}
}
