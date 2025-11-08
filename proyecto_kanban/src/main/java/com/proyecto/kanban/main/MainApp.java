package com.proyecto.kanban.main;

import javafx.application.Application;
import javafx.stage.Stage;
import com.proyecto.kanban.service.AuthService;
import com.proyecto.kanban.storage.Repository;
import com.proyecto.kanban.view.LoginView;

/**
 * Clase principal de la aplicación Kanban.
 */
public class MainApp extends Application {
    
    private AuthService authService;
    private Repository repository;
    
    @Override
    public void init() throws Exception {
        repository = new Repository();
        authService = new AuthService(repository);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Kanban Planner");
        primaryStage.setMaximized(true); // Maximizar ventana para mejor visualización
        showLoginScreen(primaryStage);
    }
    
    private void showLoginScreen(Stage stage) {
        LoginView loginView = new LoginView(authService);
        loginView.show(stage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
