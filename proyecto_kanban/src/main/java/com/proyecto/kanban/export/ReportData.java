package com.proyecto.kanban.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO simple que representa la estructura de un reporte preparado por
 * `ReportService` y consumido por `PdfReportGenerator`.
 *
 * Origen de los datos:
 * - `title` se establece desde `ReportService.buildResumenProyecto` y contiene
 *   el título del informe (por ejemplo "Resumen del proyecto: X").
 * - `metadata` es un mapa de pares clave/valor con información contextual
 *   (por ejemplo id/nombre del proyecto, periodo desde/hasta). Se usa para
 *   mostrar metadatos en el encabezado del PDF.
 * - `tables` contiene una o más instancias de `ReportTable` (p. ej. tabla de
 *   KPIs y tabla de tareas). Cada `ReportTable` tiene columnas y filas ya
 *   formateadas como `String` para evitar dependencias de presentación.
 *
 * Notas:
 * - Esta clase es un DTO de exportación; por eso está en el paquete
 *   `com.proyecto.kanban.export` y no en `model`. No contiene lógica de negocio,
 *   solo estructura de datos para la presentación/export.
 */
public class ReportData {
    // Título legible del reporte (usado en el encabezado del PDF)
    private String title;

    // Metadata contextual (ej: proyectoId, proyectoNombre, periodoDesde, periodoHasta)
    private Map<String, String> metadata = new HashMap<>();

    // Tablas del reporte. Cada ReportTable tiene columnas y filas ya serializadas a String.
    private List<ReportTable> tables = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Map<String, String> getMetadata() { return metadata; }
    public List<ReportTable> getTables() { return tables; }

    // Añade una tabla al final de la lista. Usado por ReportService.
    public void addTable(ReportTable table) { tables.add(table); }
}
