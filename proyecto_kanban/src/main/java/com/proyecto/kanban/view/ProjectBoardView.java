package com.proyecto.kanban.view;

import com.proyecto.kanban.model.*;
import com.proyecto.kanban.view.util.MiembroTableData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.List;

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
    private Button membersButton;
    // Servicios inyectados
    private final com.proyecto.kanban.service.ProjectService projectService;
    private final com.proyecto.kanban.service.TaskService taskService;
    private final com.proyecto.kanban.service.AuthService authService;
    // Controles de filtrado
    private ComboBox<Prioridad> filterPriorityCombo;
    private ComboBox<Usuario> filterAssignedCombo;
    private ComboBox<Etiqueta> filterTagCombo;
    private Button filterClearButton;

    public ProjectBoardView() {
        this(null, null, null, null);
    }

    public ProjectBoardView(Consumer<Stage> logoutHandler) {
        this(logoutHandler, null, null, null);
    }

    public ProjectBoardView(Consumer<Stage> logoutHandler,
                            com.proyecto.kanban.service.ProjectService projectService,
                            com.proyecto.kanban.service.TaskService taskService,
                            com.proyecto.kanban.service.AuthService authService) {
        this.proyectosUsuario = FXCollections.observableArrayList();
        this.tareasProyecto = FXCollections.observableArrayList();
        this.logoutHandler = logoutHandler;
        this.projectService = projectService;
        this.taskService = taskService;
        this.authService = authService;
    }

    public void show(Stage stage, Usuario usuario) {
        this.usuarioActual = usuario;
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Barra superior con información del usuario
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label userLabel = new Label("Usuario: " + usuarioActual.getNombre());
        
        // Botón para gestionar miembros (solo habilitado cuando hay un proyecto seleccionado)
        membersButton = new Button("Gestionar Miembros");
        membersButton.setDisable(true); // Inicialmente deshabilitado
        membersButton.setOnAction(e -> showMembersDialog());
        
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setOnAction(e -> {
            if (logoutHandler != null) {
                logoutHandler.accept(stage);
            } else {
                // fallback: recreate a fresh login view (may lose in-memory state)
                new LoginView(new com.proyecto.kanban.service.AuthService(new com.proyecto.kanban.storage.Repository())).show(stage);
            }
        });
        topBar.getChildren().addAll(userLabel, membersButton, logoutButton);
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

        // Barra de filtros (se inicializa aquí y se poblará cuando se seleccione un proyecto)
        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(5));

        filterPriorityCombo = new ComboBox<>();
        filterPriorityCombo.getItems().addAll(Prioridad.values());
        filterPriorityCombo.setPromptText("Prioridad (Todos)");

        filterAssignedCombo = new ComboBox<>();
        filterAssignedCombo.setPromptText("Asignado (Todos)");
        // Personalizar cómo se muestran los usuarios
        filterAssignedCombo.setCellFactory(param -> new ListCell<Usuario>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setText("Sin asignar");
                } else {
                    setText(usuario.getNombre() + " (" + usuario.getEmail() + ")");
                }
            }
        });
        filterAssignedCombo.setButtonCell(filterAssignedCombo.getCellFactory().call(null));

        filterTagCombo = new ComboBox<>();
        filterTagCombo.setPromptText("Etiqueta (Todas)");
        // Personalizar cómo se muestran las etiquetas
        filterTagCombo.setCellFactory(param -> new ListCell<Etiqueta>() {
            @Override
            protected void updateItem(Etiqueta etiqueta, boolean empty) {
                super.updateItem(etiqueta, empty);
                if (empty || etiqueta == null) {
                    setText(null);
                } else {
                    setText(etiqueta.getNombre());
                    String rawColor = etiqueta.getColor() != null ? etiqueta.getColor() : "#999999";
                    String cssColor = rawColor;
                    if (rawColor.startsWith("0x") && rawColor.length() >= 8) {
                        cssColor = "#" + rawColor.substring(2, 8);
                    }
                    if (!cssColor.startsWith("#")) cssColor = "#" + cssColor;
                    setStyle("-fx-text-fill: " + cssColor + ";");
                }
            }
        });

        filterClearButton = new Button("Limpiar");

        // Aplicar filtros automáticamente cuando cambian los valores
        filterPriorityCombo.setOnAction(e -> refreshBoard());
        filterAssignedCombo.setOnAction(e -> refreshBoard());
        filterTagCombo.setOnAction(e -> refreshBoard());

        filterClearButton.setOnAction(e -> {
            filterPriorityCombo.setValue(null);
            filterAssignedCombo.setValue(null);
            filterTagCombo.setValue(null);
            refreshBoard();
        });

        filterBar.getChildren().addAll(
            new Label("Filtrar por:"), 
            new Label("Prioridad:"), filterPriorityCombo,
            new Label("Asignado:"), filterAssignedCombo,
            new Label("Etiqueta:"), filterTagCombo,
            filterClearButton
        );
        board.getChildren().add(filterBar);

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
                // Aplicar filtros seleccionados
                Prioridad selectedPriority = filterPriorityCombo != null ? filterPriorityCombo.getValue() : null;
                Usuario selectedAssigned = filterAssignedCombo != null ? filterAssignedCombo.getValue() : null;
                Etiqueta selectedTag = filterTagCombo != null ? filterTagCombo.getValue() : null;

                tareasProyecto.stream()
                    .filter(t -> t.getEstado() == estado)
                    .filter(t -> selectedPriority == null || t.getPrioridad() == selectedPriority)
                    .filter(t -> selectedAssigned == null || (t.getAsignadoA() != null && t.getAsignadoA().equals(selectedAssigned)))
                    .filter(t -> selectedTag == null || (t.getEtiquetas() != null && t.getEtiquetas().contains(selectedTag)))
                    .forEach(tarea -> {
                        TaskCard card = new TaskCard(tarea, this::refreshBoard, taskService);
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
            if (dialogButton == createButtonType && projectService != null) {
                // Usar el servicio para crear el proyecto con el usuario actual como líder
                return projectService.crearProyecto(
                    nombreField.getText(),
                    descripcionArea.getText(),
                    usuarioActual
                );
            } else if (dialogButton == createButtonType) {
                // Fallback al comportamiento anterior si no hay servicio
                Proyecto p = new Proyecto(nombreField.getText(), descripcionArea.getText());
                p.agregarMiembro(usuarioActual); // Agregar el creador como primer miembro
                return p;
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

        // ComboBox para asignar miembro
        ComboBox<Usuario> asignadoCombo = new ComboBox<>();
        asignadoCombo.getItems().addAll(proyectoActual.getMiembros());
        asignadoCombo.setPromptText("Sin asignar");
        asignadoCombo.setCellFactory(param -> new ListCell<Usuario>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setText(null);
                } else {
                    setText(usuario.getNombre() + " (" + usuario.getEmail() + ")");
                }
            }
        });
        asignadoCombo.setButtonCell(asignadoCombo.getCellFactory().call(null));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Fecha límite:"), 0, 2);
        grid.add(fechaLimite, 1, 2);
        grid.add(new Label("Prioridad:"), 0, 3);
        grid.add(prioridadCombo, 1, 3);
        grid.add(new Label("Asignar a:"), 0, 4);
        grid.add(asignadoCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                // Retornamos una Tarea temporal; la inserción real será delegada al TaskService si existe
                return new Tarea(
                    nombreField.getText(),
                    descripcionArea.getText(),
                    asignadoCombo.getValue(), // Usuario asignado
                    new FechaLimite(fechaLimite.getValue()),
                    prioridadCombo.getValue(),
                    EstadoTarea.PENDIENTE
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tarea -> {
            // Si se inyectó TaskService, delegamos la creación/registro en el servicio.
            if (taskService != null) {
                Tarea nueva = taskService.createTask(
                    nombreField.getText(),
                    descripcionArea.getText(),
                    asignadoCombo.getValue(),
                    new FechaLimite(fechaLimite.getValue()),
                    prioridadCombo.getValue(),
                    EstadoTarea.PENDIENTE
                );
                // Mantener coherencia: registrar en TaskService y anexar al proyecto
                taskService.agregarTarea(nueva);
                if (proyectoActual != null) {
                    proyectoActual.agregarTarea(nueva);
                }
                // Actualizar la vista
                if (tareasProyecto != null) tareasProyecto.add(nueva);
            } else {
                // Comportamiento previo: añadir la tarea creada por el diálogo
                addTaskToCurrentProject(tarea);
            }
            refreshBoard();
        });
    }

    private void showProjectTasks(Proyecto proyecto) {
        this.proyectoActual = proyecto;
        tareasProyecto.clear();
        tareasProyecto.addAll(proyecto.getTareas());
        
        // Habilitar botón de miembros cuando hay proyecto seleccionado
        if (membersButton != null) {
            membersButton.setDisable(false);
        }
        
        // Poblar controles de filtrado con datos del proyecto
        if (filterAssignedCombo != null) {
            filterAssignedCombo.getItems().clear();
            // Añadir opción "Sin asignar"
            filterAssignedCombo.getItems().add(null);
            // Añadir miembros del proyecto
            filterAssignedCombo.getItems().addAll(proyecto.getMiembros());
            filterAssignedCombo.setValue(null);
            filterAssignedCombo.setDisable(false);
        }
        
        if (filterTagCombo != null) {
            filterTagCombo.getItems().clear();
            // Obtener todas las etiquetas únicas de todas las tareas
            List<Etiqueta> etiquetasUnicas = proyecto.getTareas().stream()
                .flatMap(t -> t.getEtiquetas().stream())
                .distinct()
                .collect(Collectors.toList());
            
            if (!etiquetasUnicas.isEmpty()) {
                filterTagCombo.getItems().addAll(etiquetasUnicas);
            }
            filterTagCombo.setValue(null);
            filterTagCombo.setDisable(etiquetasUnicas.isEmpty());
        }
        
        if (filterPriorityCombo != null) {
            filterPriorityCombo.setDisable(false);
            filterPriorityCombo.setValue(null);
        }
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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private Usuario findUserByEmail(String email) {
        if (authService != null) {
            try {
                return authService.login(email);
            } catch (Exception e) {
                return null;
            }
        }
        // Fallback si no hay AuthService disponible
        if (email.contains("@")) {
            String nombre = email.substring(0, email.indexOf("@"));
            return new Usuario(nombre, email);
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showMembersDialog() {
        if (proyectoActual == null) {
            showAlert("Error", "Selecciona un proyecto primero");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestionar Miembros");
        dialog.setHeaderText("Miembros del proyecto: " + proyectoActual.getNombre());

        // Botones del diálogo
        ButtonType closeButtonType = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // Contenido del diálogo
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Lista de miembros actuales con roles y opciones
        TableView<MiembroTableData> memberTable = new TableView<>();
        memberTable.setEditable(true);

        // Columna para el nombre y email
        TableColumn<MiembroTableData, String> nameColumn = new TableColumn<>("Miembro");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getUsuario().getNombre() + " (" + data.getValue().getUsuario().getEmail() + ")"
        ));
        nameColumn.setPrefWidth(200);

        // Columna para el rol
        TableColumn<MiembroTableData, RolMiembro> rolColumn = new TableColumn<>("Rol");
        rolColumn.setCellValueFactory(data -> data.getValue().rolProperty());
        rolColumn.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<RolMiembro> comboBox = new ComboBox<>();
            {
                comboBox.getItems().addAll(RolMiembro.values());
                comboBox.setOnAction(e -> {
                    if (getTableRow() != null) {
                        MiembroTableData data = (MiembroTableData) getTableRow().getItem();
                        if (data != null) {
                            data.setRol(comboBox.getValue());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(RolMiembro rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(rol);
                    setGraphic(comboBox);
                }
            }
        });
        rolColumn.setPrefWidth(120);

        // Columna para acciones (eliminar)
        TableColumn<MiembroTableData, Void> actionColumn = new TableColumn<>("Acciones");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Eliminar");
            {
                deleteButton.setOnAction(e -> {
                    MiembroTableData data = getTableRow().getItem();
                    if (data != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Confirmar eliminación");
                        confirm.setHeaderText("¿Eliminar miembro?");
                        confirm.setContentText("¿Estás seguro de que deseas eliminar a " + 
                            data.getUsuario().getNombre() + " del proyecto?");
                        
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                memberTable.getItems().remove(data);
                                if (projectService != null) {
                                    projectService.eliminarMiembro(proyectoActual, data.getUsuario());
                                } else {
                                    proyectoActual.eliminarMiembro(data.getUsuario());
                                }
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        actionColumn.setPrefWidth(100);

    memberTable.getColumns().addAll(java.util.Arrays.asList(nameColumn, rolColumn, actionColumn));
        memberTable.setItems(FXCollections.observableArrayList(
            proyectoActual.getMiembros().stream()
                .map(u -> new MiembroTableData(u, RolMiembro.EDITOR))
                .collect(Collectors.toList())
        ));

        // Campo para añadir nuevo miembro
        GridPane addMemberGrid = new GridPane();
        addMemberGrid.setHgap(10);
        addMemberGrid.setVgap(5);
        addMemberGrid.setPadding(new Insets(10, 0, 0, 0));

        TextField emailField = new TextField();
        emailField.setPromptText("Email del nuevo miembro");
        ComboBox<RolMiembro> rolCombo = new ComboBox<>();
        rolCombo.getItems().addAll(RolMiembro.values());
        rolCombo.setValue(RolMiembro.EDITOR);
        Button addButton = new Button("Añadir");

        addMemberGrid.add(new Label("Email:"), 0, 0);
        addMemberGrid.add(emailField, 1, 0);
        addMemberGrid.add(new Label("Rol:"), 2, 0);
        addMemberGrid.add(rolCombo, 3, 0);
        addMemberGrid.add(addButton, 4, 0);

        // Validación de email y búsqueda de usuario
        addButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (!isValidEmail(email)) {
                showAlert("Error", "Por favor, introduce un email válido");
                return;
            }

            // Buscar usuario en AuthService
            Usuario usuarioExistente = findUserByEmail(email);
            if (usuarioExistente == null) {
                showAlert("Error", "No se encontró ningún usuario con ese email");
                return;
            }

            // Confirmar añadir miembro
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar nuevo miembro");
            confirm.setHeaderText("Añadir nuevo miembro");
            confirm.setContentText("¿Deseas añadir a " + usuarioExistente.getNombre() + 
                " (" + email + ") como " + rolCombo.getValue() + "?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    MiembroTableData newMember = new MiembroTableData(usuarioExistente, rolCombo.getValue());
                    memberTable.getItems().add(newMember);
                    if (projectService != null) {
                        projectService.agregarMiembro(proyectoActual, usuarioExistente);
                    } else {
                        proyectoActual.agregarMiembro(usuarioExistente);
                    }
                    emailField.clear();
                }
            });
        });

        content.getChildren().addAll(
            new Label("Miembros del proyecto"),
            memberTable,
            new Separator(),
            new Label("Añadir nuevo miembro"),
            addMemberGrid
        );

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}