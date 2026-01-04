package controller;

import model.Ticket;
import util.Constantes;
import util.Serializador;
import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DAO para Ticket - operaciones CRUD y búsquedas útiles para asignación.
 */
public class TicketDAO {

	public List<Ticket> findAll() {
		return Serializador.leerLista(Constantes.TICKETS_FILE);
	}

	public Optional<Ticket> findById(String id) {
		if (id == null) {
			return Optional.empty();
		}
		return findAll().stream().filter(t -> id.equals(t.getIdTicket())).findFirst();
	}

	public boolean existsById(String id) {
		return findById(id).isPresent();
	}

	/**
	 * Crea y guarda un nuevo Ticket, generando su ID automáticamente.
	 * 
	 * @param t El ticket a guardar (su ID será ignorado y sobreescrito).
	 * @return El ticket guardado con su nuevo ID, o Optional.empty() si falla.
	 */
	public Optional<Ticket> crear(Ticket t) throws IOException {
		if (t == null) {
			return Optional.empty();
		}

		List<Ticket> lista = findAll();
		String nuevoId = generarSiguienteId(lista);
		t.setIdTicket(nuevoId);
		lista.add(t);
		Serializador.guardarLista(Constantes.TICKETS_FILE, lista);
		return Optional.of(t);
	}

	/**
	 * Actualiza un Ticket existente (busca por id).
	 * 
	 * @return Un Optional con el ticket actualizado si se encontró, o
	 *         Optional.empty() si no.
	 */
	public Optional<Ticket> actualizar(Ticket t) throws IOException {
		if (t == null || t.getIdTicket() == null) {
			return Optional.empty();
		}

		List<Ticket> lista = findAll();
		for (int i = 0; i < lista.size(); i++) {
			if (t.getIdTicket().equals(lista.get(i).getIdTicket())) {
				lista.set(i, t);
				Serializador.guardarLista(Constantes.TICKETS_FILE, lista);
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}

	/**
	 * Elimina un Ticket por id. Devuelve true si se eliminó.
	 */
	public boolean borrar(String id) throws IOException {
		if (id == null) {
			return false;
		}
		List<Ticket> lista = findAll(); // UNA lectura
		boolean removed = lista.removeIf(t -> id.equals(t.getIdTicket()));
		if (removed) {
			Serializador.guardarLista(Constantes.TICKETS_FILE, lista);
		}
		return removed;
	}

	/**
	 * Devuelve la lista de Tickets con la categoria dada (ignorando mayúsculas). Si
	 * categoria es null, devuelve lista vacía.
	 */
	public List<Ticket> findByCategoria(String categoria) {
		if (categoria == null) {
			return List.of();
		}
		return findAll().stream().filter(t -> categoria.equalsIgnoreCase(t.getCategoria()))
				.collect(Collectors.toList());
	}

	/**
	 * Devuelve la lista de Tickets con el estado dado (ignorando mayúsculas). Si
	 * estado es null, devuelve lista vacía.
	 */
	public List<Ticket> findByEstado(String estado) {
		if (estado == null) {
			return List.of();
		}
		return findAll().stream().filter(t -> estado.equalsIgnoreCase(t.getEstado())).collect(Collectors.toList());
	}

	/**
	 * Devuelve la lista de tickets con la prioridad dada (ignorando mayúsculas). Si
	 * la prioridad es null, devuelve lista vacia
	 */
	public List<Ticket> findByPrioridad(String prioridad) {
		if (prioridad == null) {
			return List.of();
		}
		return findAll().stream().filter(t -> prioridad.equalsIgnoreCase(t.getPrioridad()))
				.collect(Collectors.toList());

	}

	/**
	 * Genera el siguiente ID para un nuevo Ticket. Busca el ID más alto ("TKXXX") y
	 * le suma 1. Si no hay tickets, empieza en "TK001".
	 */
	private String generarSiguienteId(List<Ticket> tickets) {
		if (tickets == null || tickets.isEmpty()) {
			return "TK001";
		}

		int maxId = tickets.stream().map(Ticket::getIdTicket).filter(id -> id != null && id.matches("TK\\d+"))
				.mapToInt(id -> Integer.parseInt(id.substring("TK".length()))).max().orElse(0);

		return "TK" + String.format("%03d", maxId + 1);
	}

	/**
	 * Devuelve la lista de tickets asociados a un ID de cliente. Si el idCliente es
	 * null, devuelve una lista vacía.
	 */
	public List<Ticket> findByClienteId(String idCliente) {
		if (idCliente == null) {
			return List.of();
		}
		return findAll().stream().filter(t -> idCliente.equals(t.getAutorId())).collect(Collectors.toList());
	}

	/**
	 * Devuelve la lista de tickets asignados a un ID de técnico. Si el idTecnico es
	 * null, devuelve una lista vacía.
	 */
	public List<Ticket> findByTecnicoId(String idTecnico) {
		if (idTecnico == null) {
			return List.of();
		}
		return findAll().stream().filter(t -> idTecnico.equals(t.getAsignadoA())).collect(Collectors.toList());
	}

}
