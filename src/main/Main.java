package main;

import javax.swing.SwingUtilities;

import service.GestorUsuario;
import service.exceptions.UserException;
import view.LoginView;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		configurarAdminInicial();
		
		SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
	}
	
	/**
     * Verifica si existe un usuario administrador por defecto y, si no, lo crea.
     * Esto es útil para asegurar que siempre haya al menos un admin para gestionar el sistema.
     */
    private static void configurarAdminInicial() {
        GestorUsuario gestorUsuarios = new GestorUsuario();
        String adminEmail = "admin@gestortickets.com";
        String adminPass = "admin123";
        String adminNombre = "Administrador del Sistema";

        try {
            // Intentamos registrar el admin. El método lanzará una excepción si el correo ya existe.
            gestorUsuarios.registrarNuevoAdmin(adminNombre, adminEmail, adminPass);
            System.out.println("INFO: Usuario administrador por defecto creado con éxito.");
        } catch (UserException e) {
            // Si la excepción es porque el correo ya existe, es el comportamiento esperado.
            System.out.println("INFO: El usuario administrador por defecto ya existe.");
        }
    }

}
