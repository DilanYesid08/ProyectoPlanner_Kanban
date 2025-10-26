package com.proyecto.kanban.service;

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
}
