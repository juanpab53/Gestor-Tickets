package model;

import java.io.Serializable;

/**
 * Entidad Técnico: representa un usuario con rol de técnico. Hereda de Usuario
 * y añade campos específicos. Persistible (Serializable) para guardarse en
 * archivos binarios (serialización).
 */

public class Tecnico extends Usuario implements Serializable {

	private static final long serialVersionUID = 1L;

	// Campos propios de un técnico
	private String especialidad;
	private int cargaTrabajo;

	// Constructor vacío (útil para frameworks y para instanciar y luego setear)
	public Tecnico() {
		super();
		this.setRol("TECH");
		this.especialidad = "";
		this.cargaTrabajo = 0;
	}

	public Tecnico(String id, String nombre, String correo, String contrasena, String especialidad) {
		super(id, nombre, correo, contrasena, "TECH");
		this.especialidad = especialidad != null ? especialidad : "";
		this.cargaTrabajo = 0;
	}

	// Getters y Setters
	public String getEspecialidad() {
		return especialidad;
	}

	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad != null ? especialidad : "";
	}

	public int getCargaTrabajo() {
		return cargaTrabajo;
	}

	public void setCargaTrabajo(int cargaTrabajo) {
		this.cargaTrabajo = Math.max(0, cargaTrabajo);
	}

	/**
	 * Incrementa la carga de trabajo en 1.
	 */
	public void incrementarCarga() {
		this.cargaTrabajo++;
	}

	/**
	 * Decrementa la carga de trabajo en 1 (no baja de 0).
	 */
	public void disminuirCarga() {
		if (this.cargaTrabajo > 0)
			this.cargaTrabajo--;
	}

	/**
	 * utiliza el metodo equals basado en id (identidad lógica) de la clase Usuario
	 */
	@Override
	public boolean equals(Object obj) {
		// reutiliza la lógica de Usuario (id)
		return super.equals(obj);
	}

	/**
	 * utiliza el metodo hashCode basado en id (identidad lógica) de la clase
	 * Usuario
	 */
	@Override
	public int hashCode() {
		// coherente con equals: usar el hash del id del Usuario
		return super.hashCode();
	}

	/**
	 * Representación legible
	 */
	@Override
	public String toString() {
		return "Tecnico{" + "id='" + getId() + '\'' + ", nombre='" + getNombre() + '\'' + ", correo='" + getCorreo()
				+ '\'' + ", especializacion='" + especialidad + '\'' + ", cargaTrabajo=" + cargaTrabajo + '}';
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
