package com.proyecto.kanban.model;

import com.proyecto.kanban.exceptions.FechaInvalidaException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clase para representar la fecha límite de una tarea.
 * Se considera composición: cada tarea puede tener su FechaLimite; la fecha "vive"
 * asociada a la tarea.
 *
 * Incluye validaciones robustas para asegurar fechas válidas.
 */
public class FechaLimite {
    private LocalDate fecha;

    /** 
     * Crea FechaLimite desde día/mes/año con validación.
     * @throws FechaInvalidaException si la fecha no es válida
     */
    public FechaLimite(int dia, int mes, int año) {
        validarFecha(dia, mes, año);
        this.fecha = LocalDate.of(año, mes, dia);
    }

    /** Crea desde LocalDate directamente */
    public FechaLimite(LocalDate fecha) {
        if (fecha == null) throw new FechaInvalidaException("La fecha no puede estar vacía");
        this.fecha = fecha;
    }

    /**
     * Valida que la fecha sea correcta.
     * @throws FechaInvalidaException si algún componente de la fecha es inválido
     */
    public static void validarFecha(int dia, int mes, int año) {
        if (mes < 1 || mes > 12) {
            throw new FechaInvalidaException("El mes debe estar entre 1 y 12");
        }
        
        // Días máximos según el mes
        int maxDias = switch (mes) {
            case 2 -> (esAñoBisiesto(año)) ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
        
        if (dia < 1 || dia > maxDias) {
            throw new FechaInvalidaException(
                String.format("El día debe estar entre 1 y %d para el mes %d", maxDias, mes)
            );
        }

        // Validar año razonable (entre 2000 y 2100 para este ejemplo)
        if (año < 2000 || año > 2100) {
            throw new FechaInvalidaException(
                "El año debe estar entre 2000 y 2100"
            );
        }
    }

    /**
     * Determina si un año es bisiesto.
     * Un año es bisiesto si es divisible por 4, excepto si es divisible por 100,
     * a menos que también sea divisible por 400.
     */
    private static boolean esAñoBisiesto(int año) {
        return (año % 4 == 0 && año % 100 != 0) || (año % 400 == 0);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /** Devuelve true si la fecha está antes de hoy (vencida) */
    public boolean estaVencida() {
        return fecha != null && fecha.isBefore(LocalDate.now());
    }

    /** Formato legible ISO (yyyy-MM-dd). Útil para guardar como String. */
    @Override
    public String toString() {
        if (fecha == null) return "Sin fecha";
        return fecha.format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FechaLimite)) return false;
        FechaLimite that = (FechaLimite) o;
        return Objects.equals(fecha, that.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fecha);
    }
}
