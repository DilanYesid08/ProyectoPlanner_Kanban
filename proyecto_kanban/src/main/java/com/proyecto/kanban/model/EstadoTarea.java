package com.proyecto.kanban.model;

/**
 * Estados posibles de una tarea (representación canónica).
 * Usado en TareaBase.estado y para mover tareas entre columnas/estados.
 */
public enum EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA;

    @Override
    public String toString() {
        return switch (this) {
            case PENDIENTE -> "Pendiente";
            case EN_PROGRESO -> "En progreso";
            case COMPLETADA -> "Completada";
        };
    }
}
