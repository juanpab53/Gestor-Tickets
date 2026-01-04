package view;

import model.Usuario;
import service.GestorUsuario;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField emailField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JButton registerButton;
	private JLabel messageLabel;

	private final GestorUsuario gestorUsuarios;

	public LoginView() {
		this.gestorUsuarios = new GestorUsuario();

		// --- CONFIGURACIÓN DE LA VENTANA PRINCIPAL ---
		setTitle("Inicio de Sesión - Gestor de Tickets");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // Centrar en la pantalla
		setResizable(false);

		// --- PALETA DE COLORES ---
		Color backgroundColor = new Color(240, 245, 250); // Un gris azulado muy claro
		Color primaryColor = new Color(60, 90, 150); // Azul corporativo
		Color labelColor = new Color(50, 50, 50); // Gris oscuro para texto
		Color errorColor = new Color(200, 50, 50); // Rojo para errores

		// --- PANEL PRINCIPAL CON GRIDBAGLAYOUT ---
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(backgroundColor);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5); // Espaciado entre componentes
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// --- COMPONENTES DEL FORMULARIO ---

		// Etiqueta y campo de Correo
		JLabel emailLabel = new JLabel("Correo Electrónico:");
		emailLabel.setForeground(labelColor);
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(emailLabel, gbc);

		emailField = new JTextField(20);
		gbc.gridx = 1;
		gbc.gridy = 0;
		panel.add(emailField, gbc);

		// Etiqueta y campo de Contraseña
		JLabel passwordLabel = new JLabel("Contraseña:");
		passwordLabel.setForeground(labelColor);
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(passwordLabel, gbc);

		passwordField = new JPasswordField();
		gbc.gridx = 1;
		gbc.gridy = 1;
		panel.add(passwordField, gbc);

		// Botón de Ingresar
		loginButton = new JButton("Ingresar");
		loginButton.setBackground(primaryColor);
		loginButton.setForeground(Color.WHITE);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(loginButton, gbc);

		// Etiqueta para mensajes
		messageLabel = new JLabel(" ");
		messageLabel.setForeground(errorColor);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(messageLabel, gbc);

		// Botón de Registro (estilo enlace)
		registerButton = new JButton("¿No tienes cuenta? Regístrate");
		registerButton.setForeground(primaryColor);
		registerButton.setBorderPainted(false);
		registerButton.setContentAreaFilled(false);
		registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		panel.add(registerButton, gbc);

		// --- LÓGICA DE EVENTOS ---
		loginButton.addActionListener(e -> {
			String email = emailField.getText();
			String password = new String(passwordField.getPassword());

			Optional<? extends Usuario> usuarioOpt = gestorUsuarios.autenticarUsuario(email, password);

			if (usuarioOpt.isPresent()) {
				Usuario usuario = usuarioOpt.get();
                // Cerrar la ventana de login
                dispose();

                // Abrir el dashboard correspondiente según el rol
                switch (usuario.getRol().toUpperCase()) {
                    case "CLIENTE":
                        new DashboardClienteView(usuario).setVisible(true);
                        break;
                    case "ADMIN":
                        new DashboardAdminView(usuario).setVisible(true);
                        break;
                    case "TECH":
                        new DashboardTecnicoView(usuario).setVisible(true);
                        break;
                }
			} else {
				messageLabel.setText("Correo o contraseña incorrectos.");
			}
		});
		
		registerButton.addActionListener(e -> {
            // Abre la ventana de registro
            RegistroView registroView = new RegistroView(this, gestorUsuarios);
            registroView.setVisible(true);
        });

		add(panel);
	}
}
