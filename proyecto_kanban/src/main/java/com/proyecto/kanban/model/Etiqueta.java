package com.proyecto.kanban.model;

/**
 * Representa una etiqueta que puede asignarse a una tarea.
 * Ejemplo: "Urgente", "Diseño", "Revisión", etc.
 *
 * Las etiquetas son objetos ligeros (nombre + color) y se usan para categorizar tareas.
 */
public class Etiqueta {
    private String nombre;
    private String color;

    public Etiqueta(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    // Métodos Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() {
        return nombre + " (" + color + ")";
    }
}
