package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Ticket: representa un ticket de soporte. Persistible (Serializable)
 * para guardarse en archivos binarios (serialización).
 */

public class Ticket implements Serializable {

	// Identificador de la versión serializada
	private static final long serialVersionUID = 1L;

	private String id; // "TK001" (clave única)
	private String titulo; // Resumen corto del problema
	private String descripcion; // Detalles completos del problema
	private String autorId; // ID del usuario que creo el ticket
	private String asignadoA; // Id del técnico
	private String estado; // "ABIERTO", "EN_PROCESO", "CERRADO"
	private String categoria; // Ej: 'Redes', 'Soporte', 'Software'
	private String prioridad; // "BAJA","MEDIA","ALTA"
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaCierre; // null si sigue abierto

	// Constructor vacío (útil para frameworks y para instanciar y luego setear)
	public Ticket() {
		this.estado = "ABIERTO";
		this.prioridad = "MEDIA";
		this.fechaCreacion = LocalDateTime.now();
	}

	public Ticket(String idTicket, String titulo, String descripcion, String usuarioCreador, String categoria) {
		this.id = idTicket;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.autorId = usuarioCreador;
		this.asignadoA = null;
		this.estado = "ABIERTO";
		this.categoria = categoria;
		this.prioridad = "MEDIA";
		this.fechaCreacion = LocalDateTime.now();
		this.fechaCierre = null;
	}

	// Getters y Setters
	public String getIdTicket() {
		return id;
	}

	public void setIdTicket(String idTicket) {
		this.id = idTicket;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getAutorId() {
		return autorId;
	}

	public void setAutorId(String usuarioCreador) {
		this.autorId = usuarioCreador;
	}

	public String getAsignadoA() {
		return asignadoA;
	}

	public void setAsignadoA(String asignadoA) {
		this.asignadoA = asignadoA;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(String prioridad) {
		this.prioridad = prioridad;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(LocalDateTime fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	/**
	 * Indica si el ticket está asignado o cerrado.
	 * 
	 * @return
	 */
	public boolean asignado() {
		return asignadoA != null && !asignadoA.trim().isEmpty();
	}

	public boolean cerrado() {
		return "CERRADO".equalsIgnoreCase(estado);
	}

	/**
	 * Equals basados en id (identidad lógica)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Ticket ticket = (Ticket) o;
		return Objects.equals(id, ticket.id);
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
		return "Ticket{" + "id='" + id + '\'' + ", titulo='" + titulo + '\'' + ", autorId='" + autorId + '\''
				+ ", asignadoA='" + asignadoA + '\'' + ", estado='" + estado + '\'' + ", categoria='" + categoria + '\''
				+ ", prioridad='" + prioridad + '\'' + ", fechaCreacion=" + fechaCreacion + ", fechaCierre="
				+ fechaCierre + '}';
	}

}
