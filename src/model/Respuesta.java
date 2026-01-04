package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Respuesta: representa un comentario/mensaje asociado a un Ticket.
 * Persistible (Serializable) para guardarse en archivos binarios
 * (serialización).
 */
public class Respuesta implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id; // "R001" - identificador único de la respuesta
	private String ticketId; // id del Ticket al que pertenece
	private String autorId; // id del Usuario (cliente, técnico o admin) que escribió
	private String contenido; // texto del mensaje
	private LocalDateTime fecha; // timestamp de creación

	// Constructor vacío (necesario para deserialización y frameworks)
	public Respuesta() {
		this.fecha = LocalDateTime.now();
	}

	public Respuesta(String id, String ticketId, String autorId, String contenido, LocalDateTime fecha) {
		this.id = id;
		this.ticketId = ticketId;
		this.autorId = autorId;
		this.contenido = contenido;
		this.fecha = fecha != null ? fecha : LocalDateTime.now();
	}

	// Getters / Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public String getAutorId() {
		return autorId;
	}

	public void setAutorId(String autorId) {
		this.autorId = autorId;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	/**
	 * Vista corta del contenido (útil para listados).
	 */
	public String getPreview(int max) {
		if (contenido == null)
			return "";
		if (contenido.length() <= max)
			return contenido;
		return contenido.substring(0, max) + "...";
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

		Respuesta respuesta = (Respuesta) o;
		return Objects.equals(id, respuesta.id);
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
		return "Respuesta{" + "id='" + id + '\'' + ", ticketId='" + ticketId + '\'' + ", autorId='" + autorId + '\''
				+ ", fecha=" + fecha + ", contenido='" + "..." + contenido + '\'' + '}';
	}
}
