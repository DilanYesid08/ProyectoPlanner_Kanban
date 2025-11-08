package com.proyecto.kanban.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un proyecto que contiene múltiples tareas.
 *
 * Relaciones:
 * - Contiene muchas Tareas (composición): un proyecto es dueño de sus tareas.
 * - Agregación con Usuario: el proyecto mantiene una lista de miembros (usuarios) que participan.
 *
 * Nota: se ha retirado la serialización para mantener el código más simple para estudiantes.
 */
/**
 * Representa un proyecto que contiene múltiples tareas.
 *
 * Ahora {@code Proyecto} extiende {@link BaseEntity} para heredar un identificador
 * único {@code id}. Esto es un ejemplo de herencia (extends) aplicado con mínimo
 * impacto en la lógica existente.
 */
public class Proyecto extends BaseEntity {
    private String nombre;
    private String descripcion;
    private List<Tarea> tareas;
    private List<Usuario> miembros;

    public Proyecto(String nombre, Usuario lider) {
        this(nombre, "");
        if (lider != null) {
            agregarMiembro(lider);
        }
    }

    public Proyecto(String nombre, String descripcion) {
        // El identificador `id` proviene de {@link BaseEntity} y se inicializa
        // automáticamente en la declaración del campo en la clase base. No
        // es necesario invocar `super()` de forma explícita.
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tareas = new ArrayList<>();
        this.miembros = new ArrayList<>();
    }

    // Métodos principales
    /** Agrega un miembro (usuario) al proyecto. */
    public void agregarMiembro(Usuario usuario) {
        if (usuario != null && !miembros.contains(usuario)) miembros.add(usuario);
    }

    /** Añade una tarea al proyecto (composición). */
    public void agregarTarea(Tarea tarea) {
        if (tarea != null) tareas.add(tarea);
    }

    /** Muestra por consola las tareas del proyecto (uso simple en consola). */
    public void mostrarTareas() {
        for (Tarea t : tareas) {
            System.out.println(t);
        }
    }

    // Getters para integración con servicios y UI simples
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public List<Tarea> getTareas() { return tareas; }
    public List<Usuario> getMiembros() { return miembros; }

    @Override
    public String toString() {
        return String.format("Proyecto{id=%s, nombre='%s', descripcion='%s', miembros=%d, tareas=%d}",
                getId(), nombre, descripcion, miembros.size(), tareas.size());
    }
}
