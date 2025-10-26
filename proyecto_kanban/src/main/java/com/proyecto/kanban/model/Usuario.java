package com.proyecto.kanban.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un usuario del sistema.
 *
 * Relación con otras clases:
 * - Proyecto: un usuario puede pertenecer a varios proyectos (agregación). Se guardan referencias
 *   sencillas en `projectIds` para mantener el modelo simple y evitar ciclos complejos.
 * - Tarea: las tareas pueden tener un Usuario asignado (asociación).
 *
 * Nota: esta clase es un POJO simple usado en memoria durante la simulación
 * por consola. No implementa serialización ni guarda datos en disco.
 */
public class Usuario {
    private final String id;
    private String nombre;
    private String email;
    private final List<String> projectIds; // referencias a proyectos por id

    /**
     * Crea un nuevo Usuario. Se genera un UUID para el id.
     *
     * Nota sobre {@code UUID.randomUUID()}: genera un identificador único universal
     * (UUID) en forma de cadena. Se usa aquí para dar a cada usuario un id único
     * sin necesidad de depender de una base de datos que asigne ids.
     *
     * @param nombre nombre del usuario (puede ser vacío)
     * @param email email del usuario (usado para login en esta simulación)
     */
    public Usuario(String nombre, String email) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre != null ? nombre : "";
        this.email = email != null ? email : "";
        this.projectIds = new ArrayList<>();
    }

    /* Getters y setters básicos */
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /* Manejo de asociación con proyectos (solo almacena IDs) */
    /** Devuelve copia de la lista de projectIds para evitar exposición directa. */
    public List<String> getProjectIds() {
        return new ArrayList<>(projectIds);
    }

    /** Añade la referencia a un proyecto (por id) si no existe. */
    public void agregarProyecto(String projectId) {
        if (projectId != null && !projectIds.contains(projectId)) {
            projectIds.add(projectId);
        }
    }

    /** Remueve la referencia a un proyecto (por id). */
    public void removerProyecto(String projectId) {
        projectIds.remove(projectId);
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%s, nombre='%s', email='%s'}", id, nombre, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
