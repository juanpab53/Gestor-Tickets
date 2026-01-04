package view;

import controller.TecnicoDAO;
import controller.TicketDAO;
import controller.UsuarioDAO;
import model.Tecnico;
import model.Ticket;
import model.Usuario;
import service.GestorTicket;
import service.GestorUsuario;
import service.exceptions.UserException;
import service.exceptions.TicketException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashboardAdminView extends JFrame {

	private static final long serialVersionUID = 1L;

	private final Usuario admin;
	private final GestorTicket gestorTickets;
	private final GestorUsuario gestorUsuario;
	private final UsuarioDAO usuarioDAO;
	private final TecnicoDAO tecnicoDAO;

	private JTable ticketsTable, usuariosTable, tecnicosTable;
	private DefaultTableModel ticketsTableModel, usuariosTableModel, tecnicosTableModel;

	public DashboardAdminView(Usuario admin) {
		this.admin = admin;
		this.gestorTickets = new GestorTicket();
		this.gestorUsuario = new GestorUsuario();
		this.usuarioDAO = new UsuarioDAO();
		this.tecnicoDAO = new TecnicoDAO();

		setTitle("Dashboard de Administrador - " + admin.getNombre());
		setSize(1000, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// --- PESTAÑAS ---
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Gestionar Tickets", createTicketsPanel());
		tabbedPane.addTab("Gestionar Usuarios", createUsersPanel());
		tabbedPane.addTab("Gestionar Técnicos", createTechsPanel());

		add(tabbedPane);

		// Cargar datos iniciales
		cargarTodosLosTickets();
		cargarTodosLosUsuarios();
		cargarTodosLosTecnicos();
	}

	private JPanel createTicketsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "ID", "Descripción", "Estado", "Cliente", "Técnico Asignado", "Fecha Creación" };
		ticketsTableModel = new DefaultTableModel(columns, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		ticketsTable = new JTable(ticketsTableModel);
		panel.add(new JScrollPane(ticketsTable), BorderLayout.CENTER);

		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton assignButton = new JButton("Asignar Ticket");
		actionsPanel.add(assignButton);
		panel.add(actionsPanel, BorderLayout.SOUTH);

		assignButton.addActionListener(e -> {
			int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un ticket para asignar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ticketId = (String) ticketsTableModel.getValueAt(selectedRow, 0);
            List<Tecnico> tecnicos = tecnicoDAO.findAll();
            if (tecnicos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay técnicos disponibles para asignar el ticket.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] tecnicosNombres = tecnicos.stream().map(t -> t.getNombre() + " (ID: " + t.getId() + ")").toArray(String[]::new);
            String seleccion = (String) JOptionPane.showInputDialog(this, "Seleccione un técnico:", "Asignar Ticket",
                    JOptionPane.PLAIN_MESSAGE, null, tecnicosNombres, tecnicosNombres[0]);

            if (seleccion != null) {
                String tecnicoId = seleccion.substring(seleccion.indexOf("ID: ") + 4, seleccion.length() - 1);
                try {
                    gestorTickets.asignarTicket(admin, ticketId, tecnicoId);
                    JOptionPane.showMessageDialog(this, "Ticket asignado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarTodosLosTickets();
                } catch (UserException | TicketException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Asignación", JOptionPane.ERROR_MESSAGE);
                }
            }
		});

		return panel;
	}

	private JPanel createUsersPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "ID", "Nombre", "Correo", "Rol" };
		usuariosTableModel = new DefaultTableModel(columns, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		usuariosTable = new JTable(usuariosTableModel);
		panel.add(new JScrollPane(usuariosTable), BorderLayout.CENTER);

		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton createUserButton = new JButton("Crear Usuario");
		actionsPanel.add(createUserButton);
		JButton editUserButton = new JButton("Editar Usuario");
		actionsPanel.add(editUserButton);
		JButton deleteUserButton = new JButton("Eliminar Usuario");
        actionsPanel.add(deleteUserButton);
		panel.add(actionsPanel, BorderLayout.SOUTH);

		createUserButton.addActionListener(e -> {
			JTextField nombreField = new JTextField(20);
			JTextField correoField = new JTextField(20);
			JPasswordField contrasenaField = new JPasswordField(20);

			JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
			dialogPanel.add(new JLabel("Nombre:"));
			dialogPanel.add(nombreField);
			dialogPanel.add(new JLabel("Correo:"));
			dialogPanel.add(correoField);
			dialogPanel.add(new JLabel("Contraseña:"));
			dialogPanel.add(contrasenaField);

			int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Crear Nuevo Usuario (Cliente)",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if (result == JOptionPane.OK_OPTION) {
				String nombre = nombreField.getText();
				String correo = correoField.getText();
				String contrasena = new String(contrasenaField.getPassword());

				if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
					JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					gestorUsuario.registrarNuevoCliente(nombre, correo, contrasena);
					JOptionPane.showMessageDialog(this, "Usuario cliente creado exitosamente.", "Éxito",
							JOptionPane.INFORMATION_MESSAGE);
					cargarTodosLosUsuarios(); // Recargar la tabla
				} catch (UserException ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Registro",
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		editUserButton.addActionListener(e -> {
			int selectedRow = usuariosTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para editar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String userId = (String) usuariosTableModel.getValueAt(selectedRow, 0);
            usuarioDAO.findById(userId).ifPresent(usuario -> {
                JTextField nombreField = new JTextField(usuario.getNombre(), 20);
                JTextField correoField = new JTextField(usuario.getCorreo(), 20);
                String[] roles = {"CLIENTE", "ADMIN", "TECH"};
                JComboBox<String> rolComboBox = new JComboBox<>(roles);
                rolComboBox.setSelectedItem(usuario.getRol());

                JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                dialogPanel.add(new JLabel("Nombre:"));
                dialogPanel.add(nombreField);
                dialogPanel.add(new JLabel("Correo:"));
                dialogPanel.add(correoField);
                dialogPanel.add(new JLabel("Rol:"));
                dialogPanel.add(rolComboBox);

                int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Editar Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String nuevoNombre = nombreField.getText();
                    String nuevoCorreo = correoField.getText();
                    String nuevoRol = (String) rolComboBox.getSelectedItem();

                    if (nuevoNombre.isBlank() || nuevoCorreo.isBlank() || nuevoRol == null) {
                        JOptionPane.showMessageDialog(this, "Nombre, correo y rol no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        gestorUsuario.actualizarDatosUsuario(admin, userId, nuevoNombre, nuevoCorreo, nuevoRol);
                        JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        // Recargamos ambas tablas por si hubo un cambio de rol
                        cargarTodosLosUsuarios();
                        cargarTodosLosTecnicos();
                    } catch (UserException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Actualización", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
		});
		
		deleteUserButton.addActionListener(e -> {
            int selectedRow = usuariosTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String userId = (String) usuariosTableModel.getValueAt(selectedRow, 0);

            // Prevenir que el administrador se elimine a sí mismo
            if (admin.getId().equals(userId)) {
                JOptionPane.showMessageDialog(this, "No puede eliminar su propia cuenta de administrador.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar a este usuario? Esta acción no se puede deshacer.", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (usuarioDAO.borrar(userId)) {
                        JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarTodosLosUsuarios(); // Recargar la tabla
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo encontrar al usuario para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error de persistencia al eliminar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

		return panel;
	}

	private JPanel createTechsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "ID", "Nombre", "Correo", "Especialidad", "Carga de Trabajo" };
		tecnicosTableModel = new DefaultTableModel(columns, 0) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tecnicosTable = new JTable(tecnicosTableModel);
		panel.add(new JScrollPane(tecnicosTable), BorderLayout.CENTER);

		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton createTechButton = new JButton("Crear Técnico");
		actionsPanel.add(createTechButton);
		JButton editTechButton = new JButton("Editar Técnico");
        actionsPanel.add(editTechButton);
		JButton deleteTechButton = new JButton("Eliminar Técnico");
        actionsPanel.add(deleteTechButton);
		panel.add(actionsPanel, BorderLayout.SOUTH);

		createTechButton.addActionListener(e -> {
			JTextField nombreField = new JTextField(20);
			JTextField correoField = new JTextField(20);
			JPasswordField contrasenaField = new JPasswordField(20);
			JTextField especialidadField = new JTextField(20);

			JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
			dialogPanel.add(new JLabel("Nombre:"));
			dialogPanel.add(nombreField);
			dialogPanel.add(new JLabel("Correo:"));
			dialogPanel.add(correoField);
			dialogPanel.add(new JLabel("Contraseña:"));
			dialogPanel.add(contrasenaField);
			dialogPanel.add(new JLabel("Especialidad:"));
			dialogPanel.add(especialidadField);

			int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Crear Nuevo Técnico",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if (result == JOptionPane.OK_OPTION) {
				String nombre = nombreField.getText();
				String correo = correoField.getText();
				String contrasena = new String(contrasenaField.getPassword());
				String especialidad = especialidadField.getText();

				if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank() || especialidad.isBlank()) {
					JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					gestorUsuario.registrarNuevoTecnico(nombre, correo, contrasena, especialidad);
					JOptionPane.showMessageDialog(this, "Técnico creado exitosamente.", "Éxito",
							JOptionPane.INFORMATION_MESSAGE);
					cargarTodosLosTecnicos(); // Recargar la tabla
				} catch (UserException ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Registro",
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		editTechButton.addActionListener(e -> {
            int selectedRow = tecnicosTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un técnico para editar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String techId = (String) tecnicosTableModel.getValueAt(selectedRow, 0);
            tecnicoDAO.findById(techId).ifPresent(tecnico -> {
                JTextField nombreField = new JTextField(tecnico.getNombre(), 20);
                JTextField correoField = new JTextField(tecnico.getCorreo(), 20);
                JTextField especialidadField = new JTextField(tecnico.getEspecialidad(), 20);

                JPanel dialogPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                dialogPanel.add(new JLabel("Nombre:"));
                dialogPanel.add(nombreField);
                dialogPanel.add(new JLabel("Correo:"));
                dialogPanel.add(correoField);
                dialogPanel.add(new JLabel("Especialidad:"));
                dialogPanel.add(especialidadField);

                int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Editar Técnico", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String nuevoNombre = nombreField.getText();
                    String nuevoCorreo = correoField.getText();
                    String nuevaEspecialidad = especialidadField.getText();

                    if (nuevoNombre.isBlank() || nuevoCorreo.isBlank() || nuevaEspecialidad.isBlank()) {
                        JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        // Se actualiza el objeto y se persiste con el DAO
                        tecnico.setNombre(nuevoNombre);
                        tecnico.setCorreo(nuevoCorreo);
                        tecnico.setEspecialidad(nuevaEspecialidad);
                        tecnicoDAO.actualizar(tecnico);

                        JOptionPane.showMessageDialog(this, "Técnico actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarTodosLosTecnicos(); // Recargar la tabla
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error de persistencia al actualizar el técnico: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        });
		
		deleteTechButton.addActionListener(e -> {
            int selectedRow = tecnicosTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un técnico para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String techId = (String) tecnicosTableModel.getValueAt(selectedRow, 0);

            // Validar que el técnico no tenga tickets asignados
            boolean tieneTickets = new TicketDAO().findAll().stream()
                    .anyMatch(ticket -> techId.equals(ticket.getAsignadoA()) && !"CERRADO".equalsIgnoreCase(ticket.getEstado()));

            if (tieneTickets) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar al técnico porque tiene tickets activos asignados. Reasígnelos primero.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar a este técnico? Esta acción no se puede deshacer.", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (tecnicoDAO.borrar(techId)) {
                        JOptionPane.showMessageDialog(this, "Técnico eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarTodosLosTecnicos(); // Recargar la tabla
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo encontrar al técnico para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error de persistencia al eliminar el técnico: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

		return panel;
	}

	private void cargarTodosLosTickets() {
		ticketsTableModel.setRowCount(0);
		List<Ticket> tickets = new TicketDAO().findAll();
		Map<String, Usuario> userMap = usuarioDAO.findAll().stream()
				.collect(Collectors.toMap(Usuario::getId, Function.identity()));
		tecnicoDAO.findAll().forEach(t -> userMap.put(t.getId(), t));

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		for (Ticket ticket : tickets) {
			Usuario cliente = userMap.get(ticket.getAutorId());
			Usuario tecnico = userMap.get(ticket.getAsignadoA());
			Object[] row = { ticket.getIdTicket(), ticket.getDescripcion(), ticket.getEstado(),
					cliente != null ? cliente.getNombre() : "N/A",
					tecnico != null ? tecnico.getNombre() : "No asignado",
					ticket.getFechaCreacion().format(formatter) };
			ticketsTableModel.addRow(row);
		}
	}

	private void cargarTodosLosUsuarios() {
		usuariosTableModel.setRowCount(0);
		List<Usuario> usuarios = usuarioDAO.findAll();
		for (Usuario usuario : usuarios) {
			if (!"TECH".equals(usuario.getRol())) { // Excluimos técnicos de esta lista
				usuariosTableModel.addRow(
						new Object[] { usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol() });
			}
		}
	}

	private void cargarTodosLosTecnicos() {
		tecnicosTableModel.setRowCount(0);
		List<Tecnico> tecnicos = tecnicoDAO.findAll();
		for (Tecnico tecnico : tecnicos) {
			tecnicosTableModel.addRow(new Object[] { tecnico.getId(), tecnico.getNombre(), tecnico.getCorreo(),
					tecnico.getEspecialidad(), tecnico.getCargaTrabajo() });
		}
	}
}
