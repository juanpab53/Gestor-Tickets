package controller;

import model.Tecnico;
import util.Constantes;
import util.Serializador;
import java.io.IOException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DAO para Tecnico - operaciones CRUD y búsquedas útiles para asignación.
 */
public class TecnicoDAO {

	public List<Tecnico> findAll() {
		return Serializador.leerLista(Constantes.TECNICOS_FILE);
	}

	public Optional<Tecnico> findById(String id) {
		if (id == null) {
			return Optional.empty();
		}
		return findAll().stream().filter(t -> id.equals(t.getId())).findFirst();
	}

	public boolean existsById(String id) {
		return findById(id).isPresent();
	}

	/**
	 * Crea y guarda un nuevo Técnico, generando su ID automáticamente.
	 * 
	 * @param t El técnico a guardar (su ID será ignorado y sobreescrito).
	 * @return El técnico guardado con su nuevo ID, o Optional.empty() si falla.
	 */
	public Optional<Tecnico> crear(Tecnico t) throws IOException {
		if (t == null) {
			return Optional.empty();
		}
		List<Tecnico> lista = findAll();
		String nuevoId = generarSiguienteId(lista);
		t.setId(nuevoId);
		lista.add(t);
		Serializador.guardarLista(Constantes.TECNICOS_FILE, lista);
		return Optional.of(t);
	}

	/**
	 * Actualiza un técnico existente (busca por id).
	 * 
	 * @return Un Optional con el técnico actualizado si se encontró, o
	 *         Optional.empty() si no.
	 */
	public Optional<Tecnico> actualizar(Tecnico t) throws IOException {
		if (t == null || t.getId() == null) {
			return Optional.empty();
		}

		List<Tecnico> lista = findAll();
		for (int i = 0; i < lista.size(); i++) {
			if (t.getId().equals(lista.get(i).getId())) {
				lista.set(i, t);
				Serializador.guardarLista(Constantes.TECNICOS_FILE, lista);
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}

	/**
	 * Elimina un técnico por id. Devuelve true si se eliminó.
	 */
	public boolean borrar(String id) throws IOException {
		if (id == null) {
			return false;
		}
		List<Tecnico> lista = findAll(); // UNA lectura
		boolean removed = lista.removeIf(t -> id.equals(t.getId()));
		if (removed) {
			Serializador.guardarLista(Constantes.TECNICOS_FILE, lista);
		}
		return removed;
	}

	/**
	 * Devuelve la lista de técnicos con la especialización dada (ignorando
	 * mayúsculas). Si especializacion es null, devuelve lista vacía.
	 */
	public List<Tecnico> findByEspecializacion(String especializacion) {
		if (especializacion == null) {
			return List.of();
		}
		return findAll().stream().filter(t -> especializacion.equalsIgnoreCase(t.getEspecialidad()))
				.collect(Collectors.toList());
	}

	/**
	 * Devuelve el técnico con menos carga de trabajo entre los que tienen la
	 * especialización dada. Si no hay ninguno, devuelve Optional.empty(). Si
	 * especializacion es null, devuelve Optional.empty().
	 */
	public Optional<Tecnico> findLeastLoadedByEspecializacion(String especializacion) {
		if (especializacion == null) {
			return Optional.empty();
		}
		return findByEspecializacion(especializacion).stream().min(Comparator.comparingInt(Tecnico::getCargaTrabajo));
	}

	/**
	 * Genera el siguiente ID para un nuevo Técnico. Busca el ID más alto ("TXXX") y
	 * le suma 1. Si no hay técnicos, empieza en "T001".
	 */
	private String generarSiguienteId(List<Tecnico> tecnicos) {
		if (tecnicos == null || tecnicos.isEmpty()) {
			return "T001";
		}

		int maxId = tecnicos.stream().map(Tecnico::getId).filter(id -> id != null && id.matches("T\\d+"))
				.mapToInt(id -> Integer.parseInt(id.substring(1))).max().orElse(0);
		return "T" + String.format("%03d", maxId + 1);
	}
}
