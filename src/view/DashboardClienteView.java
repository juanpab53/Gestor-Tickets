package view;

import model.Ticket;
import model.Usuario;
import service.GestorTicket;
import service.exceptions.TicketException;
import service.exceptions.UserException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardClienteView extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Usuario cliente;
	private final GestorTicket gestorTickets;
	private JTable ticketsTable;
	private DefaultTableModel tableModel;

	public DashboardClienteView(Usuario cliente) {
		this.cliente = cliente;
		this.gestorTickets = new GestorTicket();

		// --- CONFIGURACIÓN DE LA VENTANA ---
		setTitle("Dashboard del Cliente - " + cliente.getNombre());
		setSize(800, 600);
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
		JLabel welcomeLabel = new JLabel("Mis Tickets", SwingConstants.CENTER);
		welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		welcomeLabel.setForeground(headerColor);
		headerPanel.add(welcomeLabel, BorderLayout.CENTER);

		JButton cerrarSesionButton = new JButton("Cerrar Sesión");
		headerPanel.add(cerrarSesionButton, BorderLayout.EAST);

		mainPanel.add(headerPanel, BorderLayout.NORTH);

		// --- TABLA DE TICKETS ---
		String[] columnNames = { "ID", "Descripción", "Estado", "Fecha de Creación" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Hacer la tabla no editable
			}
		};
		ticketsTable = new JTable(tableModel);
		ticketsTable.setFillsViewportHeight(true);
		ticketsTable.setRowHeight(25);
		ticketsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
		ticketsTable.getTableHeader().setBackground(headerColor);
		ticketsTable.getTableHeader().setForeground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(ticketsTable);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		// --- PANEL DE ACCIONES (SUR) ---
		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actionPanel.setBackground(backgroundColor);
		JButton crearTicketButton = new JButton("Crear Nuevo Ticket");
		crearTicketButton.setBackground(new Color(30, 150, 80));
		crearTicketButton.setForeground(Color.WHITE);
		actionPanel.add(crearTicketButton);
		mainPanel.add(actionPanel, BorderLayout.SOUTH);

		// --- LÓGICA DE EVENTOS ---
		cerrarSesionButton.addActionListener(e -> {
			this.dispose();
			new LoginView().setVisible(true);
		});
		
		crearTicketButton.addActionListener(e -> {
            // Crear un panel para el diálogo
            JPanel dialogPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            JTextField categoriaField = new JTextField(20);
            JTextArea descripcionArea = new JTextArea(5, 20);
            descripcionArea.setLineWrap(true);
            descripcionArea.setWrapStyleWord(true);

            dialogPanel.add(new JLabel("Categoría (ej. Software, Redes, Hardware):"));
            dialogPanel.add(categoriaField);
            dialogPanel.add(new JLabel("Descripción del problema:"));
            dialogPanel.add(new JScrollPane(descripcionArea));

            int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Crear Nuevo Ticket",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String categoria = categoriaField.getText();
                String descripcion = descripcionArea.getText();

                if (categoria.isBlank() || descripcion.isBlank()) {
                    JOptionPane.showMessageDialog(this, "La categoría y la descripción son obligatorias.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    gestorTickets.crearTicket(cliente, categoria, descripcion);
                    JOptionPane.showMessageDialog(this, "Ticket creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarTickets(); // Recargar la tabla para mostrar el nuevo ticket
                } catch (UserException | TicketException ex) {
                    JOptionPane.showMessageDialog(this, "Error al crear el ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

		// --- CARGAR DATOS INICIALES ---
		cargarTickets();

		add(mainPanel);
	}

	private void cargarTickets() {
		tableModel.setRowCount(0); // Limpiar tabla antes de cargar
		List<Ticket> tickets = gestorTickets.consultarTicketsPorCliente(cliente);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		for (Ticket ticket : tickets) {
			Object[] row = { ticket.getIdTicket(), ticket.getDescripcion(), ticket.getEstado(),
					ticket.getFechaCreacion().format(formatter) };
			tableModel.addRow(row);
		}
	}

}
