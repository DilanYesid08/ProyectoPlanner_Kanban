package com.proyecto.kanban.service;

import com.proyecto.kanban.model.Proyecto;
import com.proyecto.kanban.model.Tarea;
import com.proyecto.kanban.export.ReportData;
import com.proyecto.kanban.export.ReportTable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que construye reportes consolidados a partir de un Proyecto y un rango de fechas.
 * 
 * Propósito: permitir exportar un "Resumen Semanal/Mensual" con métricas (KPIs) clave
 * y un listado detallado de tareas. Los datos se estructuran en un `ReportData` (DTOs)
 * para que la UI o PdfReportGenerator puedan consumirlos sin conocer la lógica del modelo.
 */
public class ReportService {
    public ReportService() {
    }

    /**
     * Construye un ReportData con resumen y lista de tareas para un proyecto y periodo.
     *
     * @param proyecto el proyecto a reportar
     * @param desde fecha inicio del periodo (incluida)
     * @param hasta fecha fin del periodo (incluida)
     * @return ReportData con dos tablas: "Resumen" (KPIs) y "Tareas" (listado detallado)
     */
    public ReportData buildResumenProyecto(Proyecto proyecto, LocalDate desde, LocalDate hasta) {
        ReportData data = new ReportData();
        // Titulo y metadata
        data.setTitle("Resumen del proyecto: " + proyecto.getNombre());
        data.getMetadata().put("proyectoId", proyecto.getId());
        data.getMetadata().put("proyectoNombre", proyecto.getNombre());
        data.getMetadata().put("periodoDesde", desde.toString());
        data.getMetadata().put("periodoHasta", hasta.toString());

        // Convertir fechas a rangos de LocalDateTime para comparar
        LocalDateTime start = desde.atStartOfDay();
        LocalDateTime end = hasta.atTime(LocalTime.MAX);

        List<Tarea> tareas = proyecto.getTareas();

        // Contar tareas creadas en el periodo (usando fechaCreacion)
        long creadas = tareas.stream()
                .filter(t -> t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(start) && !t.getFechaCreacion().isAfter(end))
                .count();

        // Contar tareas completadas en el periodo (usando fechaCierre)
        long completadas = tareas.stream()
                .filter(t -> t.getFechaCierre() != null && !t.getFechaCierre().isBefore(start) && !t.getFechaCierre().isAfter(end))
                .count();

        // Contar tareas "En progreso" creadas en el periodo
        long enProgreso = tareas.stream()
                .filter(t -> t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(start) && !t.getFechaCreacion().isAfter(end))
                .filter(t -> t.getEstado() != null && t.getEstado().toString().equals("EN_PROGRESO"))
                .count();
        
        // Contar tareas "Pendiente" creadas en el periodo
        long pendientes = tareas.stream()
                .filter(t -> t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(start) && !t.getFechaCreacion().isAfter(end))
                .filter(t -> t.getEstado() != null && t.getEstado().toString().equals("PENDIENTE"))
                .count();

        // Contar tareas vencidas (creadas en el periodo y con fechaLimite pasada, no completadas)
        long vencidas = tareas.stream()
                .filter(t -> t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(start) && !t.getFechaCreacion().isAfter(end))
                .filter(t -> t.getFechaLimite() != null && t.getFechaLimite().getFecha().isBefore(hasta.plusDays(1)))
                .filter(t -> t.getEstado() != null && !t.getEstado().toString().equals("COMPLETADA"))
                .count();

        // Filtrar tareas creadas dentro del periodo para calcular progreso period-limited
        List<Tarea> tareasEnPeriodo = tareas.stream()
                .filter(t -> t.getFechaCreacion() != null && !t.getFechaCreacion().isBefore(start) && !t.getFechaCreacion().isAfter(end))
                .collect(Collectors.toList());

        // Progreso promedio sobre las tareas del periodo (0/50/100 según estado)
        double sumaPesos = tareasEnPeriodo.stream().mapToInt(Tarea::getProgressPercent).sum();
        int progreso = tareasEnPeriodo.isEmpty() ? 0 : (int) Math.round(sumaPesos / (double) tareasEnPeriodo.size());

        // --- Tabla de Resumen (KPIs) ---
        ReportTable resumen = new ReportTable("Resumen");
        resumen.setColumns(List.of("Metric", "Value"));
        resumen.addRow(List.of("Tareas creadas", String.valueOf(creadas)));
        resumen.addRow(List.of("Tareas completadas", String.valueOf(completadas)));
        resumen.addRow(List.of("En progreso", String.valueOf(enProgreso)));
        resumen.addRow(List.of("Pendientes", String.valueOf(pendientes)));
        resumen.addRow(List.of("Vencidas", String.valueOf(vencidas)));
        resumen.addRow(List.of("Progreso %", String.valueOf(progreso)));
        data.addTable(resumen);

        // --- Tabla de Tareas Detalladas ---
        // Listado de TODAS las tareas del proyecto con sus detalles formateados.
        ReportTable tareasTable = new ReportTable("Tareas");
        tareasTable.setColumns(List.of("Id","Titulo","Asignado","Prioridad","Estado","Creada","Cierre","FechaLimite","Etiquetas"));
        int idCounter = 1;
        for (Tarea t : tareas) {
            // Convertir atributos a cadenas para la tabla
            String etiquetas = t.getEtiquetas().stream().map(e -> e.getNombre()).collect(Collectors.joining(";"));
            String asignado = t.getAsignadoA() != null ? t.getAsignadoA().getNombre() : "";
            String fechaCre = t.getFechaCreacion() != null ? t.getFechaCreacion().toLocalDate().toString() : "";
            String fechaCierre = t.getFechaCierre() != null ? t.getFechaCierre().toLocalDate().toString() : "";
            String fechaLim = t.getFechaLimite() != null ? t.getFechaLimite().toString() : "";
            tareasTable.addRow(List.of(
                    String.valueOf(idCounter++),
                    t.getTitulo(),
                    asignado,
                    t.getPrioridad() != null ? t.getPrioridad().name() : "",
                    t.getEstado() != null ? t.getEstado().name() : "",
                    fechaCre,
                    fechaCierre,
                    fechaLim,
                    etiquetas
            ));
        }
        data.addTable(tareasTable);

        return data;
    }
}
