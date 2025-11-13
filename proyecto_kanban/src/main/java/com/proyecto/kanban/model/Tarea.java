package com.proyecto.kanban.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private Proyecto proyecto; // Referencia al proyecto al que pertenece la tarea
    private final LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private LocalDateTime fechaInicio;

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
        this.fechaCreacion = LocalDateTime.now();
    }

    /**
     * Constructor sobrecargado que acepta una lista inicial de etiquetas (agregación).
     * No crea nuevas etiquetas internamente; recibe referencias externas y las añade
     * usando {@link #agregarEtiqueta(Etiqueta)} para aplicar la lógica de deduplicación.
     */
    public Tarea(String titulo, String descripcion, Usuario asignadoA, FechaLimite fechaLimite,
                 Prioridad prioridad, EstadoTarea estado, List<Etiqueta> etiquetasIniciales) {
        this(titulo, descripcion, asignadoA, fechaLimite, prioridad, estado);
        if (etiquetasIniciales != null) {
            for (Etiqueta e : etiquetasIniciales) {
                this.agregarEtiqueta(e);
            }
        }
    }

    /** Fecha y hora en que se creó la tarea. */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    // Getters simples
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public Usuario getAsignadoA() { return asignadoA; }
    public FechaLimite getFechaLimite() { return fechaLimite; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoTarea getEstado() { return estado; }
    public List<Etiqueta> getEtiquetas() { return etiquetas; }
    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }

    /** Añade una etiqueta a la tarea evitando duplicados por nombre (case-insensitive). */
    public void agregarEtiqueta(Etiqueta etiqueta) {
        if (etiqueta == null) return;
        String nombreNueva = etiqueta.getNombre() != null ? etiqueta.getNombre().trim() : "";
        // Evitar etiquetas duplicadas por nombre (case-insensitive)
        boolean existe = etiquetas.stream().anyMatch(e -> {
            String n = e.getNombre() != null ? e.getNombre().trim() : "";
            return !n.isEmpty() && !nombreNueva.isEmpty() && n.equalsIgnoreCase(nombreNueva);
        });
        if (!existe) {
            etiquetas.add(etiqueta);
        }
    }

    /** Cambia el estado de la tarea (p.ej. PENDIENTE -> EN_PROGRESO -> COMPLETADA). */
    public void cambiarEstado(EstadoTarea nuevoEstado) {
        if (nuevoEstado != null) {
            this.estado = nuevoEstado;
            // Registrar fecha de cierre cuando se marca como COMPLETADA
            if (nuevoEstado == EstadoTarea.COMPLETADA) {
                this.fechaCierre = LocalDateTime.now();
            } else {
                this.fechaCierre = null;
            }
            // Registrar fechaInicio la primera vez que pasa a EN_PROGRESO
            if (nuevoEstado == EstadoTarea.EN_PROGRESO && this.fechaInicio == null) {
                this.fechaInicio = LocalDateTime.now();
            }
        }
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

    /** Modifica la fecha límite de la tarea. */
    public void setFechaLimite(FechaLimite fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    /** Fecha y hora en la que la tarea fue completada (si aplica). */
    public LocalDateTime getFechaCierre() { return fechaCierre; }

    /** Fecha y hora en la que la tarea pasó a EN_PROGRESO (si aplica). */
    public LocalDateTime getFechaInicio() { return fechaInicio; }
        /** Modifica el título de la tarea. */
        public void setTitulo(String titulo) {
            if (titulo != null && !titulo.trim().isEmpty()) {
                this.titulo = titulo.trim();
            }
        }

        /** Modifica la descripción de la tarea. */
        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion != null ? descripcion.trim() : "";
        }
    @Override
    public String toString() {
        String asignado = asignadoA != null ? asignadoA.getNombre() : "Sin asignar";
        String fechaStr = fechaLimite != null ? fechaLimite.toString() : "Sin fecha";
        String creado = fechaCreacion != null ? fechaCreacion.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "-";
        return "Tarea: " + titulo + " | Creada: " + creado + " | Asignado a: " + asignado +
               " | Prioridad: " + prioridad + " | Estado: " + estado +
               " | Fecha límite: " + fechaStr +
               " | Etiquetas: " + etiquetas;
    }
}
