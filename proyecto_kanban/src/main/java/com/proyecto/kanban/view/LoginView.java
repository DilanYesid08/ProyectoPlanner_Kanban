package com.proyecto.kanban.view;

import com.proyecto.kanban.model.Usuario;
import com.proyecto.kanban.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Vista de login y registro de usuarios.
 * Esta clase maneja la interfaz gráfica para autenticación y registro de usuarios.
 */
public class LoginView {
    private final AuthService authService;
    private ProjectBoardView projectBoardView;

    /**
     * Constructor de LoginView
     * @param authService Servicio de autenticación para manejar login/registro
     */
    public LoginView(AuthService authService) {
        this.authService = authService;
        this.projectBoardView = null;
    }

    /**
     * Muestra la ventana de login/registro
     * @param stage Stage principal de la aplicación
     */
    public void show(Stage stage) {
        VBox loginRoot = new VBox(10);
        loginRoot.setPadding(new Insets(20));

        // Título principal
        Label titleLabel = new Label("Kanban Planner");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Sección de inicio de sesión
        Label loginLabel = new Label("Iniciar Sesión");
        loginLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        Button loginButton = new Button("Iniciar Sesión");

        // Sección de registro
        Label registerLabel = new Label("Registro");
        registerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Nombre");
        TextField registerEmailField = new TextField();
        registerEmailField.setPromptText("Email");
        Button registerButton = new Button("Registrarse");

        // Configurar eventos
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), stage));
        registerButton.setOnAction(e -> handleRegister(nameField.getText(), registerEmailField.getText(), stage));

        // Agregar todos los elementos a la vista
        loginRoot.getChildren().addAll(
            titleLabel,
            new Separator(),
            loginLabel,
            emailField,
            loginButton,
            new Separator(),
            registerLabel,
            nameField,
            registerEmailField,
            registerButton
        );

        Scene loginScene = new Scene(loginRoot, 400, 500);
        stage.setScene(loginScene);
        stage.show();
    }

    /**
     * Maneja el proceso de inicio de sesión
     * @param email Email del usuario
     * @param stage Stage principal para cambiar la vista
     */
    private void handleLogin(String email, Stage stage) {
        if (email == null || email.trim().isEmpty()) {
            showError("Error de inicio de sesión", "El email no puede estar vacío");
            return;
        }

        try {
            Usuario usuario = authService.login(email);
            if (usuario != null) {
                // create ProjectBoardView with logout handler to return to login
                if (projectBoardView == null) {
                    projectBoardView = new ProjectBoardView(s -> this.show(s));
                }
                projectBoardView.show(stage, usuario);
            } else {
                showError("Error de inicio de sesión", "Usuario no encontrado");
            }
        } catch (Exception ex) {
            showError("Error de inicio de sesión", ex.getMessage());
        }
    }

    /**
     * Maneja el proceso de registro de usuarios
     * @param nombre Nombre del nuevo usuario
     * @param email Email del nuevo usuario
     * @param stage Stage principal para cambiar la vista
     */
    private void handleRegister(String nombre, String email, Stage stage) {
        if (nombre == null || nombre.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            showError("Error de registro", "El nombre y el email son obligatorios");
            return;
        }

        try {
            Usuario usuario = authService.signup(nombre, email);
            if (usuario != null) {
                if (projectBoardView == null) {
                    projectBoardView = new ProjectBoardView(s -> this.show(s));
                }
                projectBoardView.show(stage, usuario);
            } else {
                showError("Error de registro", "El email ya está registrado");
            }
        } catch (Exception ex) {
            showError("Error de registro", ex.getMessage());
        }
    }

    /**
     * Muestra un diálogo de error
     * @param title Título del error
     * @param message Mensaje de error
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}