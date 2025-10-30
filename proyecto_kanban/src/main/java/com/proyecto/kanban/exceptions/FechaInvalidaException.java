package com.proyecto.kanban.exceptions;

/**
 * Excepción personalizada para errores de validación de fechas.
 * Esta excepción se lanza cuando se intenta crear o modificar una fecha
 * con valores inválidos (por ejemplo, día 31 en febrero).
 */
public class FechaInvalidaException extends RuntimeException {
    public FechaInvalidaException(String mensaje) {
        super(mensaje);
    }
}