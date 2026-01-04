package util;

public class Constantes {

	/**
	 * Constantes del proyecto (rutas, nombres de ficheros).
	 */

	private Constantes() {
	}

	// Carpeta donde se guardan los .dat serializados (puedes cambiarla si quieres)
	public static final String DATA_DIR = "./Datos";

	// Nombres de ficheros (extensión .dat para indicar binario/serializado)
	public static final String USUARIOS_FILE = DATA_DIR + "/usuarios.txt";
	public static final String TECNICOS_FILE = DATA_DIR + "/tecnicos.txt";
	public static final String TICKETS_FILE = DATA_DIR + "/tickets.txt";
	public static final String RESPUESTAS_FILE = DATA_DIR + "/respuestas.txt";

	// Nombre temporal usado para escritura segura
	public static final String TEMP_SUFFIX = ".tmp";
}
