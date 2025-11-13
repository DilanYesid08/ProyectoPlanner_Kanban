package com.proyecto.kanban.export;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una tabla simple dentro de un `ReportData`.
 *
 * Estructura:
 * - `name`: nombre de la tabla (por ejemplo "Resumen" o "Tareas").
 * - `columns`: nombres de las columnas en orden.
 * - `rows`: lista de filas; cada fila es una lista de celdas ya serializadas a String.
 *
 * Uso:
 * - `ReportService` construye instancias de `ReportTable` añadiendo columnas y
 *   filas ya convertidas a `String` (p. ej. fechas formateadas, nombres de usuario),
 *   para que `PdfReportGenerator` pueda renderizarlas sin conocer el modelo.
 */
public class ReportTable {
    private String name;
    private List<String> columns = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();

    public ReportTable(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<String> getColumns() { return columns; }
    public List<List<String>> getRows() { return rows; }

    // Define las columnas (se espera una lista de nombres de columna)
    public void setColumns(List<String> cols) { this.columns = cols; }

    // Añade una fila; las celdas deben estar en el mismo orden que `columns`.
    public void addRow(List<String> row) { rows.add(row); }
}
