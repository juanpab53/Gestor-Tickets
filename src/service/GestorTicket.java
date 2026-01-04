package service;

import controller.TecnicoDAO;
import controller.TicketDAO;
import model.Tecnico;
import model.Ticket;
import model.Usuario;
import service.exceptions.TicketException;
import service.exceptions.UserException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Capa de servicio para la gestión de tickets. Contiene la lógica de negocio
 * relacionada con la creación, asignación, y resolución de tickets. Orquesta
 * las operaciones utilizando los DAOs.
 */
public class GestorTicket {

	private final TicketDAO ticketDAO;
	private final TecnicoDAO tecnicoDAO;

	public GestorTicket() {
		this.ticketDAO = new TicketDAO();
		this.tecnicoDAO = new TecnicoDAO();
	}

	/**
	 * Crea un nuevo ticket en el sistema.
	 *
	 * @param cliente     El usuario cliente que crea el ticket.
	 * @param categoria   La categoría del problema (ej. "Software", "Hardware").
	 * @param descripcion Una descripción detallada del problema.
	 * @return El ticket recién creado.
	 * @throws UsuarioException si el usuario no es un cliente válido.
	 * @throws TicketException  si ocurre un error al crear el ticket.
	 */
	public Ticket crearTicket(Usuario cliente, String categoria, String descripcion)
			throws UserException, TicketException {
		if (cliente == null || !"CLIENTE".equals(cliente.getRol())) {
			throw new UserException("Solo los clientes pueden crear tickets.");
		}

		Ticket nuevoTicket = new Ticket();
		nuevoTicket.setAutorId(cliente.getId());
		nuevoTicket.setCategoria(categoria);
		nuevoTicket.setDescripcion(descripcion);
		nuevoTicket.setEstado("ABIERTO");
		nuevoTicket.setFechaCreacion(LocalDateTime.now());

		try {
			return ticketDAO.crear(nuevoTicket)
					.orElseThrow(() -> new TicketException("Error interno: No se pudo guardar el ticket."));
		} catch (IOException e) {
			// Si ocurre un error de I/O, lo envolvemos en una TicketException para
			// notificar a la capa superior.
			throw new TicketException("Error de persistencia al crear el ticket: " + e.getMessage());
		}
	}

	/**
	 * Asigna un ticket abierto a un técnico. Operación solo para administradores.
	 *
	 * @param admin     El usuario administrador que realiza la asignación.
	 * @param ticketId  El ID del ticket a asignar.
	 * @param tecnicoId El ID del técnico al que se le asignará el ticket.
	 * @return El ticket actualizado.
	 * @throws UsuarioException si el usuario no es un administrador.
	 * @throws TicketException  si el ticket o el técnico no existen, o si el ticket
	 *                          no está abierto.
	 */
	public Ticket asignarTicket(Usuario admin, String ticketId, String tecnicoId)
			throws UserException, TicketException {
		if (admin == null || !"ADMIN".equals(admin.getRol())) {
			throw new UserException("Solo los administradores pueden asignar tickets.");
		}

		Ticket ticket = ticketDAO.findById(ticketId)
				.orElseThrow(() -> new TicketException("No se encontró el ticket con ID: " + ticketId));

		if (!"ABIERTO".equalsIgnoreCase(ticket.getEstado())) {
			throw new TicketException(
					"El ticket no puede ser asignado porque su estado es '" + ticket.getEstado() + "'.");
		}

		Tecnico tecnico = tecnicoDAO.findById(tecnicoId)
				.orElseThrow(() -> new TicketException("No se encontró el técnico con ID: " + tecnicoId));

		// Lógica de negocio: actualizar estado y técnico del ticket
		ticket.setAsignadoA(tecnico.getId());
		ticket.setEstado("ASIGNADO");

		// Orquestación: actualizar la carga de trabajo del técnico
		tecnico.incrementarCarga();

		try {
			// Persistir ambos cambios
			ticketDAO.actualizar(ticket);
			tecnicoDAO.actualizar(tecnico);
		} catch (IOException e) {
			throw new TicketException("Error de persistencia al asignar el ticket: " + e.getMessage());
		}

		return ticket;
	}
	/**
	 * Marca un ticket como resuelto. Operación solo para el técnico asignado.
     *
     * @param tecnico   El usuario técnico que resuelve el ticket.
     * @param ticketId  El ID del ticket a resolver.
     * @return El ticket actualizado.
     * @throws UsuarioException si el usuario no es un técnico o no es el asignado.
     * @throws TicketException  si el ticket no existe o no está en un estado válido para ser resuelto.
     */
    public Ticket resolverTicket(Usuario tecnico, String ticketId) throws UserException, TicketException {
        if (tecnico == null || !"TECH".equals(tecnico.getRol())) {
            throw new UserException("Solo los técnicos pueden resolver tickets.");
        }

        Ticket ticket = ticketDAO.findById(ticketId)
                .orElseThrow(() -> new TicketException("No se encontró el ticket con ID: " + ticketId));

        if (!tecnico.getId().equals(ticket.getAsignadoA())) {
            throw new UserException("No puede resolver un ticket que no le ha sido asignado.");
        }

        if (ticket.cerrado()) {
            throw new TicketException("El ticket ya se encuentra cerrado.");
        }

        Tecnico tecnicoAsignado = tecnicoDAO.findById(tecnico.getId())
                .orElseThrow(() -> new TicketException("Error interno: No se encontró al técnico asignado."));

        // Lógica de negocio: actualizar estado, fecha y carga de trabajo
        ticket.setEstado("CERRADO");
        ticket.setFechaCierre(LocalDateTime.now());
        tecnicoAsignado.disminuirCarga();

        try {
            ticketDAO.actualizar(ticket);
            tecnicoDAO.actualizar(tecnicoAsignado);
        } catch (IOException e) {
            throw new TicketException("Error de persistencia al resolver el ticket: " + e.getMessage());
        }
        return ticket;
    }

	/**
	 * Consulta todos los tickets asociados a un cliente específico.
	 *
	 * @param cliente El usuario cliente.
	 * @return Una lista de tickets pertenecientes a ese cliente.
	 */
	public List<Ticket> consultarTicketsPorCliente(Usuario cliente) {
		return ticketDAO.findByClienteId(cliente.getId());
	}

	/**
	 * Consulta todos los tickets asignados a un técnico específico.
	 *
	 * @param tecnico El usuario técnico.
	 * @return Una lista de tickets asignados a ese técnico.
	 */
	public List<Ticket> consultarTicketsPorTecnico(Usuario tecnico) {
		return ticketDAO.findByTecnicoId(tecnico.getId());
	}
}
