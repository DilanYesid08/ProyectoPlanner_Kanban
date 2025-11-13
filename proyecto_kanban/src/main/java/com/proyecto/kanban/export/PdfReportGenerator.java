package com.proyecto.kanban.export;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Pequeño generador de PDF que toma un `ReportData` y produce un archivo PDF.
 *
 * Notas importantes (por qué reflexión):
 * - OpenHTMLToPDF es una dependencia externa que puede introducir fricciones
 *   con `module-info.java` y el classpath en distintos entornos. Para reducir
 *   problemas a la hora de ejecutar en entornos educativos/IDE, aquí se hace
 *   uso de reflexión para invocar `PdfRendererBuilder` en tiempo de ejecución.
 * - La reflexión permite capturar y desempaquetar la causa de errores (p.ej.
 *   problemas con fuentes o parsing de HTML) y mostrar mensajes más claros.
 *
 * Flujo general:
 * 1. `ReportService` prepara un `ReportData` con metadata y tablas.
 * 2. `PdfReportGenerator.generatePdf(data, destino)` construye un HTML/XHTML
 *    sencillo (método `buildHtml`) y, usando reflection, invoca OpenHTMLToPDF
 *    para renderizar a `destino`.
 * 3. Los PDFs generados se suelen guardar en la carpeta `Informes` del
 *    directorio de trabajo del usuario; la UI informa al usuario de la ruta.
 */
public class PdfReportGenerator {

    /** Genera un PDF a partir del ReportData usando OpenHTMLToPDF vía reflexión */
    public void generatePdf(ReportData data, Path destino) throws Exception {
        if (destino == null) throw new IllegalArgumentException("Destino del PDF es null");
        String html = buildHtml(data);
        try (OutputStream os = Files.newOutputStream(destino)) {
            // Usar reflexión para evitar dependencias de módulos en tiempo de compilación
            Class<?> builderClass = Class.forName("com.openhtmltopdf.pdfboxout.PdfRendererBuilder");
            Object builder = builderClass.getDeclaredConstructor().newInstance();
            Method useFastMode = builderClass.getMethod("useFastMode");
            useFastMode.invoke(builder);
            Method withHtml = builderClass.getMethod("withHtmlContent", String.class, String.class);
            // withHtml recibe el contenido HTML y una baseUri (null si no aplica)
            withHtml.invoke(builder, html, null);
            Method toStream = builderClass.getMethod("toStream", java.io.OutputStream.class);
            toStream.invoke(builder, os);
            Method run = builderClass.getMethod("run");
            run.invoke(builder);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            // La llamada reflectiva lanzó una excepción en tiempo de ejecución: desempaquetar la causa
            Throwable cause = ite.getCause();
            if (cause != null) {
                // Re-lanzar con la causa original para que el UI muestre el mensaje real
                throw new Exception("Error al generar el PDF: " + cause.getMessage(), cause);
            } else {
                throw new Exception("Error al generar el PDF: " + ite.getMessage(), ite);
            }
        } catch (ClassNotFoundException cnfe) {
            // Si falta la dependencia en el classpath informamos claramente
            throw new IllegalStateException("Dependencia OpenHTMLToPDF no encontrada en el classpath", cnfe);
        } catch (ReflectiveOperationException roe) {
            // Métodos de reflexión (NoSuchMethod, Instantiation, IllegalAccess, etc.)
            throw new IllegalStateException("Error interno de reflexión al inicializar el generador PDF", roe);
        }
    }

    /**
     * Construye un HTML/XHTML básico a partir de ReportData.
     * - Se generan encabezados con metadata y una tabla por cada ReportTable.
     * - Las celdas se insertan ya escapadas para evitar problemas de parse.
     * Nota: el HTML es intencionalmente simple (sin recursos externos) para
     * facilitar el render y evitar dependencias en tiempo de ejecución.
     */
    private String buildHtml(ReportData data) {
        StringBuilder sb = new StringBuilder();
        // Usar XHTML 1.0 Strict para compatibilidad XML
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        sb.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"es\" lang=\"es\">\n");
        sb.append("<head>\n");
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("body { font-family: Arial, Helvetica, sans-serif; margin: 20px; }\n");
        sb.append("h1 { color: #333; }\n");
        sb.append(".meta { margin-bottom: 20px; }\n");
        sb.append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n");
        sb.append("th,td { border: 1px solid #ddd; padding: 8px; font-size: 12px; }\n");
        sb.append("th { background: #f4f4f4; text-align: left; }\n");
        sb.append(".kpi { font-weight: bold; font-size: 14px; }\n");
        sb.append("</style>\n");
        sb.append("</head>\n<body>\n");

        sb.append("<h1>").append(escapeHtml(data.getTitle())).append("</h1>");

        // Mostrar metadata como pares clave:valor
        sb.append("<div class='meta'>");
        data.getMetadata().forEach((k,v) -> sb.append("<div><strong>").append(escapeHtml(k)).append(":</strong> ").append(escapeHtml(v)).append("</div>\n"));
        sb.append("</div>");

        // Renderizar cada ReportTable en orden
        for (ReportTable table : data.getTables()) {
            sb.append("<h2>").append(escapeHtml(table.getName())).append("</h2>\n");
            sb.append("<table summary=\"").append(escapeHtml(table.getName())).append("\">\n");
            sb.append("<thead>\n<tr>\n");
            for (String col : table.getColumns()) {
                sb.append("<th scope=\"col\">").append(escapeHtml(col)).append("</th>\n");
            }
            sb.append("</tr>\n</thead>\n<tbody>\n");
            for (List<String> row : table.getRows()) {
                sb.append("<tr>\n");
                for (String cell : row) {
                    sb.append("<td>").append(escapeHtml(cell == null ? "" : cell)).append("</td>\n");
                }
                sb.append("</tr>\n");
            }
            sb.append("</tbody>\n</table>\n");
        }

        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }
}
