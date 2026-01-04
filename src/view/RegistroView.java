package view;

import service.GestorUsuario;
import service.exceptions.UserException;

import javax.swing.*;
import java.awt.*;

public class RegistroView extends JDialog {

	private static final long serialVersionUID = 1L;

	private JTextField nombreField;
	private JTextField emailField;
	private JPasswordField passwordField;
	private JPasswordField confirmPasswordField;
	private JButton registerButton;
	private JButton cancelButton;
	private JLabel messageLabel;

	private final GestorUsuario gestorUsuarios;

	public RegistroView(Frame owner, GestorUsuario gestorUsuarios) {
		super(owner, "Registro de Nuevo Cliente", true); // true para hacerlo modal
		this.gestorUsuarios = gestorUsuarios;

		// --- CONFIGURACIÓN DEL DIÁLOGO ---
		setSize(450, 400);
		setLocationRelativeTo(owner); // Centrar relativo a la ventana de login
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Cierra solo este diálogo

		// --- PALETA DE COLORES (consistente con LoginView) ---
		Color backgroundColor = new Color(240, 245, 250);
		Color primaryColor = new Color(60, 90, 150);
		Color labelColor = new Color(50, 50, 50);
		Color errorColor = new Color(200, 50, 50);
		Color successColor = new Color(30, 150, 80);

		// --- PANEL PRINCIPAL ---
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(backgroundColor);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// --- COMPONENTES DEL FORMULARIO ---

		// Nombre
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(new JLabel("Nombre Completo:"), gbc);
		nombreField = new JTextField(20);
		gbc.gridx = 1;
		gbc.gridy = 0;
		panel.add(nombreField, gbc);

		// Correo
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(new JLabel("Correo Electrónico:"), gbc);
		emailField = new JTextField();
		gbc.gridx = 1;
		gbc.gridy = 1;
		panel.add(emailField, gbc);

		// Contraseña
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(new JLabel("Contraseña:"), gbc);
		passwordField = new JPasswordField();
		gbc.gridx = 1;
		gbc.gridy = 2;
		panel.add(passwordField, gbc);

		// Confirmar Contraseña
		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(new JLabel("Confirmar Contraseña:"), gbc);
		confirmPasswordField = new JPasswordField();
		gbc.gridx = 1;
		gbc.gridy = 3;
		panel.add(confirmPasswordField, gbc);

		// Etiqueta para mensajes
		messageLabel = new JLabel(" ");
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		panel.add(messageLabel, gbc);

		// Panel para botones
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBackground(backgroundColor);

		cancelButton = new JButton("Cancelar");
		registerButton = new JButton("Registrar");
		registerButton.setBackground(primaryColor);
		registerButton.setForeground(Color.WHITE);

		buttonPanel.add(cancelButton);
		buttonPanel.add(registerButton);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		panel.add(buttonPanel, gbc);

		// --- LÓGICA DE EVENTOS ---
		cancelButton.addActionListener(e -> dispose()); // Cierra el diálogo

		registerButton.addActionListener(e -> {
			String nombre = nombreField.getText();
			String email = emailField.getText();
			String password = new String(passwordField.getPassword());
			String confirmPassword = new String(confirmPasswordField.getPassword());

			if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
				messageLabel.setForeground(errorColor);
				messageLabel.setText("Todos los campos son obligatorios.");
				return;
			}
			if (!password.equals(confirmPassword)) {
				messageLabel.setForeground(errorColor);
				messageLabel.setText("Las contraseñas no coinciden.");
				return;
			}
			try {
				gestorUsuarios.registrarNuevoCliente(nombre, email, password);
				messageLabel.setForeground(successColor);
				messageLabel.setText("¡Registro exitoso! Puede cerrar esta ventana.");
				registerButton.setEnabled(false); // Deshabilitar para no registrar dos veces
			} catch (UserException ex) {
				messageLabel.setForeground(errorColor);
				messageLabel.setText(ex.getMessage());
			}
		});

		add(panel);
	}
}
