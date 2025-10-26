package com.proyecto.kanban.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clase para representar la fecha límite de una tarea.
 * Se considera composición: cada tarea puede tener su FechaLimite; la fecha "vive"
 * asociada a la tarea.
 *
 * NOTA: si en el futuro deseas persistir este dato en JSON, puede ser necesario
 * convertir o formatear el objeto {@link java.time.LocalDate} (por ejemplo a String
 * ISO) antes de escribirlo en disco. Por ahora el proyecto funciona en memoria.
 */
public class FechaLimite {
    private LocalDate fecha;

    /** Crea FechaLimite desde día/mes/año */
    public FechaLimite(int dia, int mes, int anio) {
        this.fecha = LocalDate.of(anio, mes, dia);
    }

    /** Crea desde LocalDate directamente */
    public FechaLimite(LocalDate fecha) {
        this.fecha = fecha;
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
