package model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entidad Usuario: representa un usuario del sistema (cliente, técnico, admin).
 * Persistible (Serializable) para guardarse en archivos binarios
 * (serialización).
 */

public class Usuario implements Serializable {

	// Identificador de la versión serializada
	private static final long serialVersionUID = 1L;

	private String id; // "U001"
	private String nombre;
	private String correo;
	private String contrasena;
	private String rol; // "CLIENTE", "TECH" y "ADMIN"

	/*
	 * La serialización requiere un constructos por defecto Tambien es norma en
	 * JavaBean por facilidad para frameworks
	 */
	public Usuario() {
	}

	public Usuario(String id, String nombre, String correo, String contrasena, String rol) {
		this.id = id;
		this.nombre = nombre;
		this.correo = correo;
		this.contrasena = contrasena;
		this.rol = rol;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		if (rol != null) {
			this.rol = rol.toUpperCase().trim(); // Asegura mayúsculas y elimina espacios
		} else {
			this.rol = null;
		}
	}

	/**
	 * utiliza el metodo equals basado en id (identidad lógica) de la clase Usuario
	 */

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Usuario usuario = (Usuario) o;
		return Objects.equals(id, usuario.id);
	}

	/**
	 * Hash code basado en id (identidad lógica)
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Representación legible
	 */
	@Override
	public String toString() {
		return "Usuario{" + "id='" + id + '\'' + ", nombre='" + nombre + '\'' + ", correo='" + correo + '\'' + ", rol='"
				+ rol + '\'' + '}';
	}

}
