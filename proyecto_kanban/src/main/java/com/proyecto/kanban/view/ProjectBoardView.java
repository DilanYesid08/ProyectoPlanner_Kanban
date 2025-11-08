package com.proyecto.kanban.view;

import com.proyecto.kanban.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.function.Consumer;

/**
 * Vista principal del tablero de proyectos.
 * Muestra los proyectos del usuario y permite su gestión.
 */
public class ProjectBoardView {
    private final ObservableList<Proyecto> proyectosUsuario;
    private final ObservableList<Tarea> tareasProyecto;
    private Usuario usuarioActual;
    private Proyecto proyectoActual;
    private Consumer<Stage> logoutHandler;

    public ProjectBoardView() {
        this(null);
    }

    public ProjectBoardView(Consumer<Stage> logoutHandler) {
        this.proyectosUsuario = FXCollections.observableArrayList();
        this.tareasProyecto = FXCollections.observableArrayList();
        this.logoutHandler = logoutHandler;
    }

    public void show(Stage stage, Usuario usuario) {
        this.usuarioActual = usuario;
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Barra superior con información del usuario
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label userLabel = new Label("Usuario: " + usuarioActual.getNombre());
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setOnAction(e -> {
            if (logoutHandler != null) {
                logoutHandler.accept(stage);
            } else {
                // fallback: recreate a fresh login view (may lose in-memory state)
                new LoginView(new com.proyecto.kanban.service.AuthService(new com.proyecto.kanban.storage.Repository())).show(stage);
            }
        });
        topBar.getChildren().addAll(userLabel, logoutButton);
        root.setTop(topBar);

        // Panel izquierdo con lista de proyectos
        VBox leftPanel = createProjectListPanel();
        root.setLeft(leftPanel);

        // Panel central con el tablero Kanban
        VBox centerPanel = createKanbanBoard();
        root.setCenter(centerPanel);

        Scene scene = new Scene(root, 1200, 800);
        // Aplicar estilos CSS
        String css = "* { -fx-font-family: 'Segoe UI', Arial, sans-serif; }" +
                ".task-card { -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); }";
        scene.getStylesheets().add("data:text/css," + css.replace(" ", "%20"));
        stage.setScene(scene);
        stage.show();
    }

    private VBox createProjectListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: #f0f0f0;");

        Label titleLabel = new Label("Proyectos");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button addProjectButton = new Button("+ Nuevo Proyecto");
        addProjectButton.setMaxWidth(Double.MAX_VALUE);
        addProjectButton.setOnAction(e -> showNewProjectDialog());

        ListView<Proyecto> projectListView = new ListView<>(proyectosUsuario);
        projectListView.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(projectListView, Priority.ALWAYS);
        
        // Personalizar cómo se muestran los proyectos
        projectListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Proyecto proyecto, boolean empty) {
                super.updateItem(proyecto, empty);
                if (empty || proyecto == null) {
                    setText(null);
                } else {
                    setText(proyecto.getNombre());
                }
            }
        });
        
        projectListView.setOnMouseClicked(event -> {
            Proyecto selected = projectListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showProjectTasks(selected);
            }
        });

        panel.getChildren().addAll(titleLabel, addProjectButton, projectListView);
        return panel;
    }

    private VBox createKanbanBoard() {
        VBox board = new VBox(10);
        board.setPadding(new Insets(10));

        // Cabecera del tablero
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label boardTitle = new Label("Tablero Kanban");
        boardTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button addTaskButton = new Button("+ Nueva Tarea");
        addTaskButton.setOnAction(e -> showNewTaskDialog());
        header.getChildren().addAll(boardTitle, addTaskButton);

        // Columnas del tablero
        HBox columns = new HBox(20);
        columns.setPadding(new Insets(10));
        columns.setAlignment(Pos.CENTER);

        VBox pendingColumn = createKanbanColumn("Pendiente", EstadoTarea.PENDIENTE);
        VBox inProgressColumn = createKanbanColumn("En Progreso", EstadoTarea.EN_PROGRESO);
        VBox completedColumn = createKanbanColumn("Completada", EstadoTarea.COMPLETADA);

        columns.getChildren().addAll(pendingColumn, inProgressColumn, completedColumn);
        board.getChildren().addAll(header, columns);

        return board;
    }

    private VBox createKanbanColumn(String title, EstadoTarea estado) {
        VBox column = new VBox(10);
        column.setPrefWidth(300);
        column.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        column.setPadding(new Insets(10));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            ScrollPane scrollPane = new ScrollPane();
            VBox tasksContainer = new VBox(10);
            tasksContainer.setPadding(new Insets(5));
            tasksContainer.setSpacing(10);
            tasksContainer.setPrefWidth(280);  // Un poco menos que el ancho de la columna para el scroll
        
            scrollPane.setContent(tasksContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(500);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
            // Observar cambios en las tareas y actualizar la vista
            tareasProyecto.addListener((javafx.collections.ListChangeListener.Change<? extends Tarea> change) -> {
                updateTasksInColumn(tasksContainer, estado);
            });
        
            // Carga inicial de tareas
            updateTasksInColumn(tasksContainer, estado);

            column.getChildren().addAll(titleLabel, scrollPane);

        return column;
    }

        private void updateTasksInColumn(VBox container, EstadoTarea estado) {
            container.getChildren().clear();
            tareasProyecto.stream()
                .filter(t -> t.getEstado() == estado)
                .forEach(tarea -> {
                    TaskCard card = new TaskCard(tarea, this::refreshBoard);
                    container.getChildren().add(card);
                });
        }
    private void showNewProjectDialog() {
        Dialog<Proyecto> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Proyecto");
        dialog.setHeaderText("Crear nuevo proyecto");

        ButtonType createButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del proyecto");
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Proyecto(nombreField.getText(), descripcionArea.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(proyecto -> {
            proyectosUsuario.add(proyecto);
        });
    }

    private void showNewTaskDialog() {
        if (proyectoActual == null) {
            showAlert("Error", "Selecciona un proyecto primero");
            return;
        }

        Dialog<Tarea> dialog = new Dialog<>();
        dialog.setTitle("Nueva Tarea");
        dialog.setHeaderText("Crear nueva tarea");

        ButtonType createButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre de la tarea");
        
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");
        
        DatePicker fechaLimite = new DatePicker();
        
        ComboBox<Prioridad> prioridadCombo = new ComboBox<>();
        prioridadCombo.getItems().addAll(Prioridad.values());
        prioridadCombo.setValue(Prioridad.MEDIA);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Fecha límite:"), 0, 2);
        grid.add(fechaLimite, 1, 2);
        grid.add(new Label("Prioridad:"), 0, 3);
        grid.add(prioridadCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Tarea(
                    nombreField.getText(),
                    descripcionArea.getText(),
                    null, // Usuario asignado
                    new FechaLimite(fechaLimite.getValue()),
                    prioridadCombo.getValue(),
                    EstadoTarea.PENDIENTE
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tarea -> {
            addTaskToCurrentProject(tarea);
        });
    }

    private void showProjectTasks(Proyecto proyecto) {
        this.proyectoActual = proyecto;
        tareasProyecto.clear();
        tareasProyecto.addAll(proyecto.getTareas());
    }

    private void addTaskToCurrentProject(Tarea tarea) {
        if (proyectoActual != null && tarea != null) {
            proyectoActual.agregarTarea(tarea);
            tareasProyecto.add(tarea);
        }
    }
    
    private void refreshBoard() {
        if (proyectoActual != null) {
            tareasProyecto.clear();
            tareasProyecto.addAll(proyectoActual.getTareas());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}