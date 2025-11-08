package com.proyecto.kanban.view;

import com.proyecto.kanban.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;


public class TaskCard extends VBox {
    private final Tarea tarea;
    
    public TaskCard(Tarea tarea, Runnable onTaskUpdated) {
        this.tarea = tarea;
        
        setPadding(new Insets(10));
        setSpacing(5);
        getStyleClass().add("task-card");
        setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Título de la tarea
    Label titleLabel = new Label(tarea.getTitulo());
    titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000;");
        
        // Indicador de prioridad
        Circle prioridadIndicator = new Circle(5);
        prioridadIndicator.setFill(getPrioridadColor(tarea.getPrioridad()));
        
        // Fecha límite si existe
        Label dateLabel = new Label();
        if (tarea.getFechaLimite() != null) {
            dateLabel.setText("Fecha límite: " + tarea.getFechaLimite().toString());
            dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
        }
        
        // Etiquetas
        FlowPane tagsPane = new FlowPane();
        tagsPane.setHgap(5);
        tagsPane.setVgap(5);
        for (Etiqueta etiqueta : tarea.getEtiquetas()) {
            Label tagLabel = new Label(etiqueta.getNombre());
            String rawColor = etiqueta.getColor() != null ? etiqueta.getColor() : "#999999";
            String cssColor = rawColor;
            if (rawColor.startsWith("0x") && rawColor.length() >= 8) {
                cssColor = "#" + rawColor.substring(2, 8);
            }
            // fallback ensure starts with #
            if (!cssColor.startsWith("#")) cssColor = "#" + cssColor;
            tagLabel.setStyle(
                "-fx-background-color: " + cssColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 2 5;" +
                "-fx-background-radius: 3;"
            );
            tagsPane.getChildren().add(tagLabel);
        }
        
        // Botón de editar
        Button editButton = new Button("Editar");
        editButton.setOnAction(e -> showEditDialog(onTaskUpdated));
        
        // Layout
        HBox header = new HBox(5, prioridadIndicator, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        
        getChildren().addAll(header, dateLabel, tagsPane, editButton);
    }
    
    private Color getPrioridadColor(Prioridad prioridad) {
        if (prioridad == null) return Color.GRAY;
        return switch (prioridad) {
            case URGENTE -> Color.RED;
            case IMPORTANTE -> Color.ORANGE;
            case MEDIA -> Color.YELLOW;
            case BAJA -> Color.GREEN;
        };
    }
    
    private void showEditDialog(Runnable onTaskUpdated) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Tarea");
        dialog.setHeaderText("Modificar " + tarea.getTitulo());
        
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField titleField = new TextField(tarea.getTitulo());
        TextArea descArea = new TextArea(tarea.getDescripcion());
        ComboBox<EstadoTarea> estadoCombo = new ComboBox<>();
        estadoCombo.getItems().addAll(EstadoTarea.values());
        estadoCombo.setValue(tarea.getEstado());
        
        ComboBox<Prioridad> prioridadCombo = new ComboBox<>();
        prioridadCombo.getItems().addAll(Prioridad.values());
        prioridadCombo.setValue(tarea.getPrioridad());
        
        DatePicker fechaPicker = new DatePicker();
        if (tarea.getFechaLimite() != null) {
            fechaPicker.setValue(tarea.getFechaLimite().getFecha());
        }
        
        // Campo para nueva etiqueta
        TextField nuevaEtiqueta = new TextField();
        Button addTagButton = new Button("Añadir Etiqueta");
        ColorPicker colorPicker = new ColorPicker(Color.BLUE);
        
        addTagButton.setOnAction(e -> {
            String nombre = nuevaEtiqueta.getText().trim();
            if (!nombre.isEmpty()) {
                String colorHex = colorPicker.getValue().toString();
                tarea.agregarEtiqueta(new Etiqueta(nombre, colorHex));
                nuevaEtiqueta.clear();
            }
        });
        
        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Estado:"), 0, 2);
        grid.add(estadoCombo, 1, 2);
        grid.add(new Label("Prioridad:"), 0, 3);
        grid.add(prioridadCombo, 1, 3);
        grid.add(new Label("Fecha límite:"), 0, 4);
        grid.add(fechaPicker, 1, 4);
        grid.add(new Label("Nueva etiqueta:"), 0, 5);
        
        HBox tagBox = new HBox(5, nuevaEtiqueta, colorPicker, addTagButton);
        grid.add(tagBox, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                tarea.setTitulo(titleField.getText());
                tarea.setDescripcion(descArea.getText());
                tarea.cambiarEstado(estadoCombo.getValue());
                tarea.setPrioridad(prioridadCombo.getValue());
                if (fechaPicker.getValue() != null) {
                    tarea.setFechaLimite(new FechaLimite(fechaPicker.getValue()));
                }
                onTaskUpdated.run();
                return dialogButton;
            }
            return null;
        });
        
        dialog.showAndWait();
    }
}