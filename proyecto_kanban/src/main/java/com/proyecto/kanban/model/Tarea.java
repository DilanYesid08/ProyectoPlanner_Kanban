package com.proyecto.kanban.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una tarea dentro del sistema Kanban.
 *
 * Relaciones con otras clases:
 * - Usuario: asociación. La tarea puede tener un Usuario asignado (responsable).
 * - FechaLimite: composición. La fecha vive con la tarea.
 * - Etiqueta: agregación. Una tarea puede contener varias etiquetas describiendo
 *   categorías, colores o estados adicionales.
 *
 * Nota: se ha eliminado la serialización de la clase para mantener el proyecto simple
 * y centrado en una simulación por consola.
 */
public class Tarea {
    private String titulo;
    private String descripcion;
    private Usuario asignadoA;
    private FechaLimite fechaLimite;
    private Prioridad prioridad;
    private EstadoTarea estado;
    private List<Etiqueta> etiquetas;

    /**
     * Constructor principal de Tarea.
     *
     * Notas sobre {@code @param}:
     * - En Java, los tags {@code @param} dentro de un Javadoc sirven para describir
     *   cada parámetro del método o constructor. Son útiles para generar documentación
     *   automática (javadoc) y para que otros desarrolladores entiendan qué esperar.
     *
     * @param titulo nombre breve de la tarea (p. ej. "Diseñar logo")
     * @param descripcion descripción detallada sobre la tarea; puede ser vacía
     * @param asignadoA usuario responsable. Puede ser {@code null} si aún no hay asignado.
     * @param fechaLimite fecha de vencimiento (composición). Puede ser {@code null} para indicar sin fecha.
     * @param prioridad prioridad de la tarea (ver enum {@link Prioridad}).
     * @param estado estado inicial (ver enum {@link EstadoTarea}).
     */
    public Tarea(String titulo, String descripcion, Usuario asignadoA, FechaLimite fechaLimite,
                 Prioridad prioridad, EstadoTarea estado) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.asignadoA = asignadoA;
        this.fechaLimite = fechaLimite;
        this.prioridad = prioridad;
        this.estado = estado;
        this.etiquetas = new ArrayList<>();
    }

    // Getters simples
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public Usuario getAsignadoA() { return asignadoA; }
    public FechaLimite getFechaLimite() { return fechaLimite; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoTarea getEstado() { return estado; }
    public List<Etiqueta> getEtiquetas() { return etiquetas; }

    /** Añade una etiqueta a la tarea. */
    public void agregarEtiqueta(Etiqueta etiqueta) {
        if (etiqueta != null) etiquetas.add(etiqueta);
    }

    /** Cambia el estado de la tarea (p.ej. PENDIENTE -> EN_PROGRESO -> COMPLETADA). */
    public void cambiarEstado(EstadoTarea nuevoEstado) {
        if (nuevoEstado != null) this.estado = nuevoEstado;
    }

    /** Asigna un usuario responsable a la tarea. */
    public void asignarUsuario(Usuario usuario) {
        this.asignadoA = usuario;
    }

    /** Cambia la prioridad de la tarea. */
    public void setPrioridad(Prioridad prioridad) {
        if (prioridad != null) this.prioridad = prioridad;
    }

    /**
     * Devuelve un progreso estimado según el estado:
     * - PENDIENTE -> 0%
     * - EN_PROGRESO -> 50%
     * - COMPLETADA -> 100%
     */
    public int getProgressPercent() {
        if (estado == null) return 0;
        return switch (estado) {
            case PENDIENTE -> 0;
            case EN_PROGRESO -> 50;
            case COMPLETADA -> 100;
        };
    }

    @Override
    public String toString() {
        String asignado = asignadoA != null ? asignadoA.getNombre() : "Sin asignar";
        String fechaStr = fechaLimite != null ? fechaLimite.toString() : "Sin fecha";
        return "Tarea: " + titulo + " | Asignado a: " + asignado +
               " | Prioridad: " + prioridad + " | Estado: " + estado +
               " | Fecha límite: " + fechaStr +
               " | Etiquetas: " + etiquetas;
    }
}
