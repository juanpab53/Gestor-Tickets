package service;

import service.exceptions.UserException;
import controller.TecnicoDAO;
import controller.UsuarioDAO;
import model.Tecnico;
import model.Usuario;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Capa de servicio para la gestión de usuarios. Contiene la lógica de negocio
 * relacionada con usuarios, como el registro, la autenticación y la gestión de
 * roles. Orquesta las operaciones utilizando los DAOs correspondientes.
 */
public class GestorUsuario {

	private final UsuarioDAO usuarioDAO;
	private final TecnicoDAO tecnicoDAO;

	/**
	 * Constructor que inicializa los DAOs necesarios.
	 */
	public GestorUsuario() {
		this.usuarioDAO = new UsuarioDAO();
		this.tecnicoDAO = new TecnicoDAO();
	}

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

	/**
	 * Autentica un usuario basado en su correo y contraseña.
	 *
	 * @param correo     El correo electrónico del usuario.
	 * @param contrasena La contraseña en texto plano.
	 * @return Un Optional que contiene el Usuario si la autenticación es exitosa,
	 *         de lo contrario, un Optional vacío.
	 */
	public Optional<? extends Usuario> autenticarUsuario(String correo, String contrasena) {
		if (correo == null || contrasena == null || correo.isBlank() || contrasena.isBlank()) {
			return Optional.empty();
		}

		if (!EMAIL_PATTERN.matcher(correo).matches()) {
			return Optional.empty();
		}

		Optional<? extends Usuario> usuarioOpt = usuarioDAO.findAll().stream()
				.filter(u -> correo.equalsIgnoreCase(u.getCorreo())).findFirst();

		if (usuarioOpt.isEmpty()) {
			usuarioOpt = tecnicoDAO.findAll().stream().filter(t -> correo.equalsIgnoreCase(t.getCorreo())).findFirst();
		}
		return usuarioOpt.filter(usuario -> contrasena.equals(usuario.getContrasena()))
				// Hacemos un cast para que el tipo de retorno sea Optional<Usuario>
				.map(usuario -> (Usuario) usuario);
	}

	/**
	 * Registra un nuevo usuario cliente en el sistema.
	 *
	 * @param nombre     El nombre del usuario.
	 * @param correo     El correo electrónico (debe ser único).
	 * @param contrasena La contraseña en texto plano.
	 * @return Un Optional con el nuevo usuario si el registro fue exitoso, o un
	 *         Optional vacío si el correo ya existe.
	 * @throws UsuarioException si el correo ya está registrado.
	 */
	public Optional<Usuario> registrarNuevoCliente(String nombre, String correo, String contrasena)
			throws UserException {
		validarCorreoNoExistente(correo);

		if (!EMAIL_PATTERN.matcher(correo).matches()) {
			throw new UserException("El formato del correo electrónico no es válido.");
		}

		Usuario nuevoUsuario = new Usuario();
		nuevoUsuario.setNombre(nombre);
		nuevoUsuario.setCorreo(correo);
		nuevoUsuario.setContrasena(contrasena);
		nuevoUsuario.setRol("CLIENTE");

		try {
			return usuarioDAO.crear(nuevoUsuario);
		} catch (IOException e) {
			throw new UserException("Error de persistencia al registrar el cliente: " + e.getMessage());
		}
	}

	/**
	 * Registra un nuevo técnico en el sistema.
	 *
	 * @param nombre       El nombre del técnico.
	 * @param correo       El correo electrónico (debe ser único).
	 * @param contrasena   La contraseña en texto plano.
	 * @param especialidad La especialidad del técnico (ej. "Software", "Redes").
	 * @return Un Optional con el nuevo técnico si el registro fue exitoso.
	 * @throws UsuarioException si el correo ya está registrado.
	 */
	public Optional<Tecnico> registrarNuevoTecnico(String nombre, String correo, String contrasena, String especialidad)
			throws UserException {
		validarCorreoNoExistente(correo);

		if (!EMAIL_PATTERN.matcher(correo).matches()) {
			throw new UserException("El formato del correo electrónico no es válido.");
		}

		Tecnico nuevoTecnico = new Tecnico();
		nuevoTecnico.setNombre(nombre);
		nuevoTecnico.setCorreo(correo);
		nuevoTecnico.setContrasena(contrasena);
		nuevoTecnico.setEspecialidad(especialidad);
		// El rol y la carga de trabajo se establecen en el constructor de Tecnico.

		try {
			return tecnicoDAO.crear(nuevoTecnico);
		} catch (IOException e) {
			throw new UserException("Error de persistencia al registrar el técnico: " + e.getMessage());
		}
	}

	/**
	 * Registra un nuevo usuario administrador en el sistema.
	 *
	 * @param nombre     El nombre del administrador.
	 * @param correo     El correo electrónico (debe ser único).
	 * @param contrasena La contraseña en texto plano.
	 * @return Un Optional con el nuevo usuario si el registro fue exitoso, o un
	 *         Optional vacío si el correo ya existe.
	 * @throws UsuarioException si el correo ya está registrado.
	 */
	public Optional<Usuario> registrarNuevoAdmin(String nombre, String correo, String contrasena) throws UserException {
		validarCorreoNoExistente(correo);

		if (!EMAIL_PATTERN.matcher(correo).matches()) {
			throw new UserException("El formato del correo electrónico no es válido.");
		}

		Usuario nuevoAdmin = new Usuario();
		nuevoAdmin.setNombre(nombre);
		nuevoAdmin.setCorreo(correo);
		nuevoAdmin.setContrasena(contrasena);
		nuevoAdmin.setRol("ADMIN");

		try {
			return usuarioDAO.crear(nuevoAdmin);
		} catch (IOException e) {
			throw new UserException("Error de persistencia al registrar el administrador: " + e.getMessage());
		}
	}

	/**
	 * Actualiza los datos de un usuario existente. Esta operación solo puede ser
	 * realizada por un administrador.
	 *
	 * @param admin                El usuario que realiza la operación (debe tener
	 *                             rol "ADMIN").
	 * @param idUsuarioAActualizar El ID del usuario a modificar.
	 * @param nuevoNombre          El nuevo nombre para el usuario.
	 * @param nuevoCorreo          El nuevo correo para el usuario.
	 * @param nuevoRol             El nuevo rol para el usuario.
	 * @return Un Optional con el usuario actualizado si la operación fue exitosa, o
	 *         un Optional vacío si el usuario no se encuentra.
	 * @throws UsuarioException si ocurre un error de negocio (permisos, correo
	 *                          duplicado).
	 */
	public Optional<Usuario> actualizarDatosUsuario(Usuario admin, String idUsuarioAActualizar, String nuevoNombre,
			String nuevoCorreo, String nuevoRol) throws UserException {
		// 1. Validación de permisos
		if (admin == null || !"ADMIN".equals(admin.getRol())) {
			throw new UserException("Error de autorización: Solo los administradores pueden actualizar usuarios.");
		}

		// 2. Encontrar el usuario a modificar
		Optional<Usuario> usuarioOpt = usuarioDAO.findById(idUsuarioAActualizar);
		if (usuarioOpt.isEmpty()) {
			return Optional.empty();
		}
		Usuario usuarioAActualizar = usuarioOpt.get();

		// 3. Validar si el nuevo correo ya está en uso por OTRO usuario
		if (nuevoCorreo != null && !nuevoCorreo.equalsIgnoreCase(usuarioAActualizar.getCorreo())) {
			if (!EMAIL_PATTERN.matcher(nuevoCorreo).matches()) {
				throw new UserException("El formato del nuevo correo electrónico no es válido.");
			}
			validarCorreoNoExistente(nuevoCorreo);
		}

		// Validar que el nuevo rol sea uno de los permitidos
		if (nuevoRol != null && !nuevoRol.trim().toUpperCase().matches("^(CLIENTE|TECH|ADMIN)$")) {
			throw new UserException("El rol '" + nuevoRol + "' no es válido. Roles permitidos: CLIENTE, TECH, ADMIN.");
		}

		// 4. Lógica de negocio: Manejar cambio de rol a Técnico
		// Si el rol cambia a "TECH" y antes no lo era, debemos crear un Técnico.
		try {
			if ("TECH".equalsIgnoreCase(nuevoRol) && !(usuarioAActualizar instanceof Tecnico)) {
				tecnicoDAO.borrar(idUsuarioAActualizar); // Borramos la instancia de Tecnico (si existiera por error)
				usuarioDAO.borrar(idUsuarioAActualizar); // Borramos la instancia de Usuario

				Tecnico tecnico = new Tecnico(idUsuarioAActualizar, nuevoNombre, nuevoCorreo,
						usuarioAActualizar.getContrasena(), "Indefinida");
				tecnico.setRol(nuevoRol); // El setter ya se encarga de estandarizar
				return tecnicoDAO.crear(tecnico).map(t -> t); // Convertimos Optional<Tecnico> a Optional<Usuario>
			}

			// 5. Actualizar los datos del usuario existente
			usuarioAActualizar.setNombre(nuevoNombre);
			usuarioAActualizar.setCorreo(nuevoCorreo);
			usuarioAActualizar.setRol(nuevoRol);

			// 6. Persistir los cambios usando el DAO correspondiente
			return usuarioDAO.actualizar(usuarioAActualizar);
		} catch (IOException e) {
			throw new UserException("Error de persistencia al actualizar el usuario: " + e.getMessage());
		}
	}

	/**
	 * Método privado de ayuda para verificar si un correo ya está en uso. Lanza una
	 * excepción si el correo ya existe.
	 * 
	 * @param correo El correo a verificar.
	 * @throws UsuarioException si el correo ya está en uso.
	 */
	private void validarCorreoNoExistente(String correo) throws UserException {
		// La búsqueda por email es ineficiente. Una mejora sería tener un método en el
		// DAO.
		if (usuarioDAO.findAll().stream().anyMatch(u -> correo.equalsIgnoreCase(u.getCorreo()))) {
			throw new UserException("Error: El correo '" + correo + "' ya está registrado.");
		}
	}
}
