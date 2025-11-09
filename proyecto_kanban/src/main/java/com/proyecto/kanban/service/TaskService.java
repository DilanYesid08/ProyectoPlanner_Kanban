package com.proyecto.kanban.service;

import java.util.List;
import java.util.ArrayList;
import com.proyecto.kanban.model.Etiqueta;
import com.proyecto.kanban.model.EstadoTarea;
import com.proyecto.kanban.model.FechaLimite;
import com.proyecto.kanban.model.Prioridad;
import com.proyecto.kanban.model.Tarea;
import com.proyecto.kanban.model.Usuario;

public class TaskService {
    /** Servicio liviano con operaciones sobre tareas. */
    public TaskService() {}

    /** Crea una tarea en memoria (no persiste por sí sola). */
    public Tarea createTask(String titulo, String descripcion, Usuario asignado,
                            FechaLimite fechaLimite, Prioridad prioridad, EstadoTarea estado) {
        return new Tarea(titulo, descripcion, asignado, fechaLimite, prioridad, estado);
    }

    /** Añade una etiqueta simple a la tarea. */
    public void addEtiqueta(Tarea tarea, String nombre, String color) {
        if (tarea != null) tarea.agregarEtiqueta(new Etiqueta(nombre, color));
    }

    /** Mueve la tarea a un nuevo estado. */
    public void moveToEstado(Tarea tarea, EstadoTarea estado) {
        if (tarea != null) tarea.cambiarEstado(estado);
    }

    /** Actualiza campos de la tarea de forma centralizada. */
    public void updateTask(Tarea tarea, String titulo, String descripcion, FechaLimite fechaLimite,
                           Prioridad prioridad, EstadoTarea estado, Usuario asignado) {
        if (tarea == null) return;
        if (titulo != null) tarea.setTitulo(titulo);
        if (descripcion != null) tarea.setDescripcion(descripcion);
        if (estado != null) tarea.cambiarEstado(estado);
        if (prioridad != null) tarea.setPrioridad(prioridad);
        if (fechaLimite != null) tarea.setFechaLimite(fechaLimite);
        // asignado puede ser null para desasignar
        tarea.asignarUsuario(asignado);
    }

    /** Lista de tareas en memoria. Usada para la interfaz gráfica. */
    private List<Tarea> tareas = new ArrayList<>();

    /** Agrega una tarea a la lista. */
    public void agregarTarea(Tarea tarea) {
        if (tarea != null) {
            tareas.add(tarea);
        }
    }

    /** Obtiene todas las tareas almacenadas. */
    public List<Tarea> obtenerTareas() {
        return new ArrayList<>(tareas); // Devuelve una copia para evitar modificaciones directas
    }
}
