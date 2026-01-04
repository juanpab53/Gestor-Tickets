package view;

import controller.UsuarioDAO;
import controller.TicketDAO;
import model.Ticket;
import model.Usuario;
import model.Respuesta;
import service.GestorTicket;
import service.GestorRespuesta;
import service.exceptions.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashboardTecnicoView extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final Usuario tecnico;
	private final GestorTicket gestorTickets;
	private final GestorRespuesta gestorRespuestas;
	private final UsuarioDAO usuarioDAO;
	private JTable ticketsTable;
	private DefaultTableModel tableModel;

	public DashboardTecnicoView(Usuario tecnico) {
		this.tecnico = tecnico;
		 this.gestorRespuestas = new GestorRespuesta();
		this.gestorTickets = new GestorTicket();
		this.usuarioDAO = new UsuarioDAO();

		// --- CONFIGURACIÓN DE LA VENTANA ---
		setTitle("Dashboard de Técnico - " + tecnico.getNombre());
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// --- PALETA DE COLORES ---
		Color backgroundColor = new Color(240, 245, 250);
		Color headerColor = new Color(60, 90, 150);

		// --- PANEL PRINCIPAL ---
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBackground(backgroundColor);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// --- CABECERA ---
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(backgroundColor);
		JLabel welcomeLabel = new JLabel("Mis Tickets Asignados", SwingConstants.CENTER);
		welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		welcomeLabel.setForeground(headerColor);
		headerPanel.add(welcomeLabel, BorderLayout.CENTER);

		JButton cerrarSesionButton = new JButton("Cerrar Sesión");
		headerPanel.add(cerrarSesionButton, BorderLayout.EAST);
		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// --- TABLA DE TICKETS ---
		String[] columnNames = { "ID", "Descripción", "Estado", "Cliente", "Fecha de Creación" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		ticketsTable = new JTable(tableModel);
		mainPanel.add(new JScrollPane(ticketsTable), BorderLayout.CENTER);

		// --- PANEL DE ACCIONES ---
		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actionPanel.setBackground(backgroundColor);
		JButton viewDetailsButton = new JButton("Ver Detalles / Responder");
        actionPanel.add(viewDetailsButton);
        JButton markResolvedButton = new JButton("Marcar como Resuelto"); 
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
		mainPanel.add(actionPanel, BorderLayout.SOUTH);

		// --- LÓGICA DE EVENTOS ---
		cerrarSesionButton.addActionListener(e -> {
			this.dispose();
			new LoginView().setVisible(true);
		});
		
		viewDetailsButton.addActionListener(e -> {
            int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un ticket para ver los detalles.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ticketId = (String) tableModel.getValueAt(selectedRow, 0);
            TicketDAO ticketDAO = new TicketDAO(); // Instancia local para obtener el ticket
            ticketDAO.findById(ticketId).ifPresentOrElse(ticket -> {
                // Crear el diálogo de detalles
                JDialog detailsDialog = new JDialog(this, "Detalles del Ticket: " + ticket.getIdTicket(), true);
                detailsDialog.setSize(600, 500);
                detailsDialog.setLocationRelativeTo(this);
                detailsDialog.setLayout(new BorderLayout(10, 10));
                detailsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                // Panel de información del ticket
                JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                infoPanel.setBorder(BorderFactory.createTitledBorder("Información del Ticket"));
                infoPanel.add(new JLabel("ID:"));
                infoPanel.add(new JLabel(ticket.getIdTicket()));
                infoPanel.add(new JLabel("Descripción:"));
                infoPanel.add(new JLabel(ticket.getDescripcion()));
                infoPanel.add(new JLabel("Estado:"));
                infoPanel.add(new JLabel(ticket.getEstado()));
                infoPanel.add(new JLabel("Cliente:"));
                Usuario cliente = usuarioDAO.findById(ticket.getAutorId()).orElse(null);
                infoPanel.add(new JLabel(cliente != null ? cliente.getNombre() : "Desconocido"));
                infoPanel.add(new JLabel("Fecha Creación:"));
                infoPanel.add(new JLabel(ticket.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

                detailsDialog.add(infoPanel, BorderLayout.NORTH);

                // Área para mostrar respuestas
                JTextArea responsesArea = new JTextArea(10, 40);
                responsesArea.setEditable(false);
                responsesArea.setLineWrap(true);
                responsesArea.setWrapStyleWord(true);
                JScrollPane responsesScrollPane = new JScrollPane(responsesArea);
                responsesScrollPane.setBorder(BorderFactory.createTitledBorder("Respuestas"));
                detailsDialog.add(responsesScrollPane, BorderLayout.CENTER);

                // Panel para añadir nueva respuesta
                JPanel newResponsePanel = new JPanel(new BorderLayout(5, 5));
                newResponsePanel.setBorder(BorderFactory.createTitledBorder("Añadir Respuesta"));
                JTextArea newResponseText = new JTextArea(3, 40);
                newResponseText.setLineWrap(true);
                newResponseText.setWrapStyleWord(true);
                JScrollPane newResponseScrollPane = new JScrollPane(newResponseText);
                JButton sendResponseButton = new JButton("Enviar Respuesta");

                newResponsePanel.add(newResponseScrollPane, BorderLayout.CENTER);
                newResponsePanel.add(sendResponseButton, BorderLayout.EAST);
                detailsDialog.add(newResponsePanel, BorderLayout.SOUTH);

                // Función para cargar y mostrar respuestas
                Runnable loadResponses = () -> {
                    responsesArea.setText(""); // Limpiar antes de cargar
                    List<Respuesta> respuestas = gestorRespuestas.consultarRespuestasPorTicket(ticketId);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    for (Respuesta r : respuestas) {
                        Usuario autorRespuesta = usuarioDAO.findById(r.getAutorId()).orElse(null);
                        String autorNombre = autorRespuesta != null ? autorRespuesta.getNombre() : "Desconocido";
                        responsesArea.append(String.format("[%s] %s (%s):\n%s\n\n",
                                r.getFecha().format(formatter), autorNombre, autorRespuesta.getRol(), r.getContenido()));
                    }
                };

                loadResponses.run(); // Cargar respuestas iniciales

                sendResponseButton.addActionListener(sendEvent -> {
                    String contenido = newResponseText.getText();
                    if (contenido.isBlank()) {
                        JOptionPane.showMessageDialog(detailsDialog, "La respuesta no puede estar vacía.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        gestorRespuestas.anadirRespuesta(tecnico, ticketId, contenido);
                        newResponseText.setText(""); // Limpiar campo
                        loadResponses.run(); // Recargar respuestas
                        JOptionPane.showMessageDialog(detailsDialog, "Respuesta enviada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } catch (TicketException | UserException | RespuestaException ex) {
                        JOptionPane.showMessageDialog(detailsDialog, ex.getMessage(), "Error al enviar respuesta", JOptionPane.ERROR_MESSAGE);
                    }
                });

                detailsDialog.setVisible(true);

            }, () -> {
                JOptionPane.showMessageDialog(this, "No se encontró el ticket seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            });
        });

		markResolvedButton.addActionListener(e -> {
            int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un ticket para marcar como resuelto.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ticketId = (String) tableModel.getValueAt(selectedRow, 0);
            String estadoActual = (String) tableModel.getValueAt(selectedRow, 2);

            if ("CERRADO".equalsIgnoreCase(estadoActual)) {
                JOptionPane.showMessageDialog(this, "Este ticket ya se encuentra cerrado.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea marcar este ticket como resuelto?", "Confirmar Acción", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    gestorTickets.resolverTicket(tecnico, ticketId);
                    JOptionPane.showMessageDialog(this, "Ticket marcado como resuelto exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarTicketsAsignados(); // Recargar la tabla para actualizar el estado
                } catch (TicketException | UserException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al resolver ticket", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
		
		// --- CARGAR DATOS ---
		cargarTicketsAsignados();

		add(mainPanel);
	}

	private void cargarTicketsAsignados() {
		tableModel.setRowCount(0);
		List<Ticket> tickets = gestorTickets.consultarTicketsPorTecnico(tecnico);
		Map<String, Usuario> userMap = usuarioDAO.findAll().stream()
				.collect(Collectors.toMap(Usuario::getId, Function.identity()));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		for (Ticket ticket : tickets) {
			Usuario cliente = userMap.get(ticket.getAutorId());
			Object[] row = { ticket.getIdTicket(), ticket.getDescripcion(), ticket.getEstado(),
					cliente != null ? cliente.getNombre() : "N/A", ticket.getFechaCreacion().format(formatter) };
			tableModel.addRow(row);
		}
	}
}
