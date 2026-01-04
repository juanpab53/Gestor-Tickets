package controller;

import model.Respuesta;
import util.Constantes;
import util.Serializador;
import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RespuestaDAO {

	public List<Respuesta> findAll() {
		return Serializador.leerLista(Constantes.RESPUESTAS_FILE);
	}

	public Optional<Respuesta> findById(String id) {
		if (id == null) {
			return Optional.empty();
		}
		return findAll().stream().filter(r -> id.equals(r.getId())).findFirst();
	}

	public boolean existsById(String id) {
		return findById(id).isPresent();
	}

	/**
	 * Crea y guarda una nueva Respuesta, generando su ID automáticamente.
	 * 
	 * @param r La respuesta a guardar (su ID será ignorado y sobreescrito).
	 * @return La respuesta guardada con su nuevo ID, o Optional.empty() si falla.
	 */
	public Optional<Respuesta> crear(Respuesta r) throws IOException {
		if (r == null) {
			return Optional.empty();
		}
		List<Respuesta> lista = findAll();
		String nuevoId = generarSiguienteId(lista);
		r.setId(nuevoId);
		lista.add(r);
		Serializador.guardarLista(Constantes.RESPUESTAS_FILE, lista);
		return Optional.of(r);
	}

	/**
	 * Actualiza una Respuesta existente (busca por id).
	 * 
	 * @return Un Optional con la respuesta actualizada si se encontró, o
	 *         Optional.empty() si no.
	 */
	public Optional<Respuesta> actualizar(Respuesta r) throws IOException {
		if (r == null || r.getId() == null) {
			return Optional.empty();
		}

		List<Respuesta> lista = findAll();
		for (int i = 0; i < lista.size(); i++) {
			if (r.getId().equals(lista.get(i).getId())) {
				lista.set(i, r);
				Serializador.guardarLista(Constantes.RESPUESTAS_FILE, lista);
				return Optional.of(r);
			}
		}
		return Optional.empty();
	}

	/**
	 * Elimina una Respuesta por id. Devuelve true si se eliminó.
	 */
	public boolean borrar(String id) throws IOException {
		if (id == null) {
			return false;
		}
		List<Respuesta> lista = findAll();
		boolean removed = lista.removeIf(r -> id.equals(r.getId()));
		if (removed) {
			Serializador.guardarLista(Constantes.RESPUESTAS_FILE, lista);
		}
		return removed;
	}

	/**
	 * Devuelve la lista de Respuestas asociadas a un Ticket específico. Si ticketId
	 * es null, devuelve lista vacía.
	 */
	public List<Respuesta> findByTicketId(String ticketId) {
		if (ticketId == null) {
			return List.of();
		}
		return findAll().stream().filter(r -> ticketId.equals(r.getTicketId())).collect(Collectors.toList());
	}

	/**
	 * Genera el siguiente ID para una nueva Respuesta. Busca el ID más alto
	 * ("RXXX") y le suma 1. Si no hay respuestas, empieza en "R001".
	 */
	private String generarSiguienteId(List<Respuesta> respuestas) {
		if (respuestas == null || respuestas.isEmpty()) {
			return "R001";
		}

		int maxId = respuestas.stream().map(Respuesta::getId).filter(id -> id != null && id.matches("R\\d+"))
				.mapToInt(id -> Integer.parseInt(id.substring(1))).max().orElse(0);
		return "R" + String.format("%03d", maxId + 1);
	}
}
