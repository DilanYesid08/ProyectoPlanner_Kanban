package com.proyecto.kanban.model;

/**
 * Prioridad de una tarea.
 * Niveles: 1 = URGENTE, 2 = IMPORTANTE, 3 = MEDIA, 4 = BAJA
 * Se usa en Tarea/TareaBase para filtrar/ordenar.
 */
public enum Prioridad {
    URGENTE(1, "Urgente"),
    IMPORTANTE(2, "Importante"),
    MEDIA(3, "Media"),
    BAJA(4, "Baja");

    private final int nivel;
    private final String etiqueta;

    Prioridad(int nivel, String etiqueta) {
        this.nivel = nivel;
        this.etiqueta = etiqueta;
    }

    public int getNivel() {
        return nivel;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
