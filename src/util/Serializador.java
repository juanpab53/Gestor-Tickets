package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Se usa para leer/escribir listas de objetos serializables en archivos.
 *
 * - Usa un enfoque read-modify-write (ideal para acceso secuencial). - Realiza
 * escritura atómica (escribe a temp file y luego mueve). - Intenta bloquear el
 * archivo durante la operación de escritura para evitar corrupciones.
 */
public final class Serializador {

	private Serializador() {
	}

	/**
	 * Asegura que exista la carpeta DATA_DIR.
	 */
	public static void asegurarDirectorioDatos() throws IOException {
		if (Files.notExists(Paths.get(Constantes.DATA_DIR))) {
			Files.createDirectories(Paths.get(Constantes.DATA_DIR));
		}
	}

	/**
	 * Lee una lista de objetos desde el archivo serializado. Si el archivo no
	 * existe o está vacío devuelve una lista vacía.
	 *
	 * @param path ruta del archivo (ej. Constantes.TICKETS_FILE)
	 * @param <T>  tipo de los elementos de la lista
	 * @return List<T> (nunca null). Devuelve lista vacía en caso de error o si no
	 *         existe el archivo.
	 */
	@SuppressWarnings("unchecked") // Porque en tiempo de ejecución (runtime) Java no sabe realmente de qué tipo es
									// la lista que estamos leyendo del archivo
	public static <T> List<T> leerLista(String path) {
		try {
			asegurarDirectorioDatos();

			Path p = Paths.get(path);
			if (Files.notExists(p) || Files.size(p) == 0) {
				return new ArrayList<>();
			}

			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(p)))) {
				return (List<T>) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			System.err.println("Error al leer o procesar el archivo: " + path + ". Se devuelve una lista vacía. Causa: "
					+ e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * Guarda una lista de objetos serializables en el archivo indicado.
	 *
	 * @param path  Ruta del archivo donde se guardará la lista.
	 * @param lista Lista de objetos a guardar.
	 */
	public static <T extends Serializable> void guardarLista(String path, List<T> lista) throws IOException {
		asegurarDirectorioDatos();
		Path filePath = Paths.get(path);
		Path tempPath = Paths.get(path + Constantes.TEMP_SUFFIX);

		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tempPath))) {
			oos.writeObject(lista);
			Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Añade un objeto a la lista persistida (operación conveniente: lee lista,
	 * añade elemento, escribe lista). NOTA: bajo concurrencia entre
	 * procesos/instancias múltiples, esta operación no es transaccional.
	 *
	 * @param <T>  tipo del objeto
	 * @param path ruta del fichero
	 * @param item objeto a añadir
	 */
	public static <T extends Serializable> void agregarABase(String path, T item) throws IOException {
		List<T> lista = leerLista(path);
		lista.add(item);
		guardarLista(path, lista);
	}

	/**
	 * Elimina todos los datos (usa con precaución, útil para pruebas).
	 */
	public static <T extends Serializable> void vaciarArchivo(String path) throws IOException {
		guardarLista(path, new ArrayList<T>());
	}

}
