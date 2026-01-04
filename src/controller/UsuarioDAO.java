package controller;

import model.Usuario;
import util.Constantes;
import util.Serializador;
import java.io.IOException;

import java.util.List;
import java.util.Optional;

/**
 * DAO para Usuario. Maneja persistencia en Constantes.USUARIOS_FILE
 * Operaciones: findAll, findById, existsById, save (create), update, deleteById
 */
public class UsuarioDAO {

	public List<Usuario> findAll() {
		return Serializador.leerLista(Constantes.USUARIOS_FILE);
	}

	public Optional<Usuario> findById(String id) {
		if (id == null) {
			return Optional.empty();
		}
		return findAll().stream().filter(u -> id.equals(u.getId())).findFirst();
	}

	public boolean existsById(String id) {
		return findById(id).isPresent();
	}

	/**
	 * Crea y guarda un nuevo Usuario, generando su ID automáticamente.
	 * 
	 * @param usuario El usuario a guardar (su ID será ignorado y sobreescrito).
	 * @return El usuario guardado con su nuevo ID, o Optional.empty() si falla.
	 */
	public Optional<Usuario> crear(Usuario usuario) throws IOException {
		if (usuario == null) {
			return Optional.empty();
		}
		List<Usuario> lista = findAll();
		String nuevoId;
		if (usuario.getRol().toUpperCase() == "ADMIN") {
			nuevoId = generarSiguienteIdAdmin(lista);
		}else {
			nuevoId = generarSiguienteId(lista);
		}
		usuario.setId(nuevoId);
		lista.add(usuario);
		Serializador.guardarLista(Constantes.USUARIOS_FILE, lista);
		return Optional.of(usuario);
	}

	/**
	 * Actualiza un usuario existente (busca por id).
	 * 
	 * @return Un Optional con el usuario actualizado si se encontró, o
	 *         Optional.empty() si no.
	 */
	public Optional<Usuario> actualizar(Usuario usuario) throws IOException {
		if (usuario == null || usuario.getId() == null) {
			return Optional.empty();
		}

		List<Usuario> lista = findAll();
		for (int i = 0; i < lista.size(); i++) {
			if (usuario.getId().equals(lista.get(i).getId())) {
				lista.set(i, usuario);
				Serializador.guardarLista(Constantes.USUARIOS_FILE, lista);
				return Optional.of(usuario);
			}
		}
		return Optional.empty();
	}

	/**
	 * Elimina un usuario por id. Devuelve true si se eliminó.
	 */
	public boolean borrar(String id) throws IOException {
		if (id == null) {
			return false;
		}

		List<Usuario> lista = findAll();
		boolean removed = lista.removeIf(u -> id.equals(u.getId()));
		if (removed) {
			Serializador.guardarLista(Constantes.USUARIOS_FILE, lista);
		}
		return removed;
	}

	/**
	 * Genera el siguiente ID para un nuevo Usuario. Busca el ID más alto ("UXXX") y
	 * le suma 1. Si no hay usuarios, empieza en "U001".
	 */
	private String generarSiguienteId(List<Usuario> usuarios) {
		if (usuarios == null || usuarios.isEmpty()) {
			return "U001";
		}

		int maxId = usuarios.stream().map(Usuario::getId).filter(id -> id != null && id.matches("U\\d+"))
				.mapToInt(id -> Integer.parseInt(id.substring(1))).max().orElse(0);

		return "U" + String.format("%03d", maxId + 1);
	}

	/**
	 * Genera el siguiente ID para un nuevo Administrador. Busca el ID más alto
	 * ("AXXX") y le suma 1. Si no hay usuarios, empieza en "U001".
	 */
	private String generarSiguienteIdAdmin(List<Usuario> usuarios) {
		if (usuarios == null || usuarios.isEmpty()) {
			return "A001";
		}

		int maxId = usuarios.stream().map(Usuario::getId).filter(id -> id != null && id.matches("A\\d+"))
				.mapToInt(id -> Integer.parseInt(id.substring(1))).max().orElse(0);

		return "A" + String.format("%03d", maxId + 1);
	}
}
