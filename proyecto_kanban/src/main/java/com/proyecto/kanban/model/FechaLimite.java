package com.proyecto.kanban.model;

import com.proyecto.kanban.exceptions.FechaInvalidaException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clase para representar la fecha limite de una tarea.
 * Se considera composicion: cada tarea puede tener su FechaLimite; la fecha "vive"
 * asociada a la tarea.
 *
 * Incluye validaciones robustas para asegurar fechas validas.
 */
public class FechaLimite {
    private LocalDate fecha;

    /** 
     * Crea FechaLimite desde dia/mes/anio con validacion.
     * @throws FechaInvalidaException si la fecha no es valida
     */
    public FechaLimite(int dia, int mes, int anio) {
        validarFecha(dia, mes, anio);
        this.fecha = LocalDate.of(anio, mes, dia);
    }

    /** Crea desde LocalDate directamente */
    public FechaLimite(LocalDate fecha) {
        if (fecha == null) throw new FechaInvalidaException("La fecha no puede estar vacia");
        this.fecha = fecha;
    }

    /**
     * Valida que la fecha sea correcta.
     * @throws FechaInvalidaException si algun componente de la fecha es invalido
     */
    public static void validarFecha(int dia, int mes, int anio) {
        if (mes < 1 || mes > 12) {
            throw new FechaInvalidaException("El mes debe estar entre 1 y 12");
        }
        
        // Dias maximos segun el mes
        int maxDias = switch (mes) {
            case 2 -> (esAnioBisiesto(anio)) ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
        
        if (dia < 1 || dia > maxDias) {
            throw new FechaInvalidaException(
                String.format("El dia debe estar entre 1 y %d para el mes %d", maxDias, mes)
            );
        }

        // Validar anio razonable (entre 2000 y 2100 para este ejemplo)
        if (anio < 2000 || anio > 2100) {
            throw new FechaInvalidaException(
                "El anio debe estar entre 2000 y 2100"
            );
        }
    }

    /**
     * Determina si un anio es bisiesto.
     * Un anio es bisiesto si es divisible por 4, excepto si es divisible por 100,
     * a menos que tambien sea divisible por 400.
     */
    private static boolean esAnioBisiesto(int anio) {
        return (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /** Devuelve true si la fecha esta antes de hoy (vencida) */
    public boolean estaVencida() {
        return fecha != null && fecha.isBefore(LocalDate.now());
    }

    /** Formato legible ISO (yyyy-MM-dd). Util para guardar como String. */
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
