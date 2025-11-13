# Guía UML, Interfaces (código vs UI), y Estructura del Proyecto

Fecha: 12 de noviembre de 2025  
Propósito: aclarar qué es un UML, qué clases merecen estar en él, y diferenciar "interfaz gráfica" de "interfaz de código" (implements).

---

## ¿Qué es un UML y cuál es su propósito?

**UML = Unified Modeling Language** (Lenguaje de Modelado Unificado)

Es un estándar para **visualizar la arquitectura de un software**. Un diagrama UML de clases te permite:

1. **Ver la estructura**: qué clases existen, qué atributos tienen, qué métodos públicos ofrecen.
2. **Entender relaciones**: cómo se conectan las clases (herencia, composición, agregación, asociación).
3. **Comunicar el diseño**: a otros desarrolladores (o a un tribunal en una defensa) sin necesidad de leer toda el código.
4. **Validar la arquitectura**: identificar si el diseño es coherente, si hay clases huérfanas, si hay ciclos de dependencia.

**En resumen:** un UML es un "mapa conceptual" del código, no un sustituto del código. Debe ser *simple, legible y relevante*.

---

## Análisis de paquetes y clases de este proyecto

Tu proyecto está distribuido así:

```
com.proyecto.kanban
├── model/           (dominio: Tarea, Proyecto, Usuario, Etiqueta, FechaLimite, etc.)
├── service/         (lógica de negocio: AuthService, ProjectService, TaskService, ReportService)
├── storage/         (acceso a datos: Repository)
├── view/            (interfaz gráfica/UI: ProjectBoardView, LoginView, TaskCard)
│   └── util/        (helpers de UI: MiembroTableData)
├── export/          (generación de reportes: ReportData, ReportTable, PdfReportGenerator)
├── util/            (utilidades: ConsoleUtil)
├── main/            (punto de entrada: MainApp)
└── exceptions/      (excepciones custom: FechaInvalidaException)
```

### Criterios para decidir qué va en el UML

No todas las clases merecen estar en el UML. Usa estos criterios:

1. **Clases del dominio** (que representan conceptos del negocio): Sí van (Tarea, Proyecto, Usuario, etc.).
2. **Servicios principales** (que orquestan lógica): Sí van (AuthService, ProjectService, TaskService, ReportService).
3. **Almacenamiento** (Repository): Sí va (es crítico para entender cómo fluyen los datos).
4. **DTOs simples** (como ReportData/ReportTable): Opcionales (depende si quieres enfatizar la capa de export).
5. **UI/Views** (ProjectBoardView, LoginView): Generalmente NO (son detalles de presentación; el UML del dominio/negocio no les interesa).
6. **Helpers/Utilities** (ConsoleUtil, MiembroTableData): NO (son detalles de implementación).
7. **Excepciones custom**: Opcionales (a menos que sean muy importantes para el flujo).
8. **Main**: NO (es solo el punto de entrada).

---

## ¿Qué clases SÍ deben estar en tu UML? (Recomendación final)

Para una defensa clara y coherente, incluye **solo el dominio + servicios + storage**:

### Paquete `model` (el corazón del dominio)
- `BaseEntity` (abstracta, da id único)
- `Usuario` (extiende BaseEntity)
- `Proyecto` (extiende BaseEntity)
- `Tarea` (sin herencia, pero central)
- `Etiqueta` (objeto valor)
- `FechaLimite` (objeto valor)
- `Prioridad` (enum)
- `EstadoTarea` (enum)
- `RolMiembro` (enum, si lo usas en relaciones)

### Paquete `service` (orquestación)
- `AuthService` (signup/login)
- `ProjectService` (crear/editar proyectos, gestionar miembros)
- `TaskService` (crear/editar tareas)
- `ReportService` (generar reportes) — opcional pero recomendado si enfatizas exportación

### Paquete `storage`
- `Repository` (gestión de datos en memoria)

### Paquete `export` (opcional, si enfatizas reportes)
- `ReportData` (DTO)
- `ReportTable` (DTO)
- `PdfReportGenerator` (genrador de PDFs)

---

## ¿Qué NO va en el UML?

- `ProjectBoardView`, `LoginView`, `TaskCard` (detalles de UI/JavaFX)
- `MiembroTableData` (es un wrapper para la tabla, no es concepto del dominio)
- `ConsoleUtil` (utility pura de I/O)
- `MainApp` (punto de entrada)
- `FechaInvalidaException` (excepción simple)

**Razón:** estos no representan la arquitectura conceptual del negocio; son detalles de cómo se presenta o se maneja la entrada/salida.

---

## Relaciones clave a mostrar en el UML

Si incluyes las clases recomendadas, estas son las relaciones principales:

### Herencia (extends)
- `Usuario extends BaseEntity`
- `Proyecto extends BaseEntity`

### Composición (1 --* ownership fuerte)
- Proyecto *-- Tarea (el proyecto "posee" sus tareas)
- Tarea *-- FechaLimite (la tarea "posee" su fecha límite)

### Agregación (o-- "tiene" sin ownership fuerte)
- Proyecto o-- Usuario (miembros)
- Tarea o-- Etiqueta (etiquetas)
- Repository o-- Usuario / Proyecto (almacena colecciones)

### Asociación (uso/referencia)
- Tarea -- Usuario : asignadoA (una tarea puede tener un usuario asignado, 0..1)
- Usuario -- Proyecto (por ids en projectIds; es una asociación débil)

### Dependencia (uso transitorio)
- AuthService → Repository (consulta/modifica usuarios)
- ProjectService → Repository (consulta/modifica proyectos)
- TaskService → Repository (indirectamente, via Proyecto)
- ReportService → Proyecto/Tarea (lee datos para reportes)
- PdfReportGenerator → ReportData (consume para generar PDF)

---

## Diferencia: "Interfaz gráfica" vs "Interfaz de código" (implements)

### Interfaz gráfica (UI)
Es lo que **ves cuando ejecutas la aplicación**:
- Botones, campos de texto, tablas, ventanas.
- En tu proyecto: `ProjectBoardView` (pantalla principal con tablero Kanban), `LoginView` (pantalla de login), `TaskCard` (tarjetas de tareas en el tablero).
- Se implementa con **JavaFX** en tu caso.
- **No aparece en el UML del dominio** porque es un detalles de presentación.

### Interfaz de código (interface/implements)
Es un **contrato** que define qué métodos debe tener una clase:

```java
// Ejemplo (NO en tu proyecto, pero ilustrativo):
public interface Entity {
    String getId();
}

public class Usuario implements Entity {
    @Override
    public String getId() { return id; }
}
```

**¿Por qué NO hay interfaces en tu proyecto?**
- Tu proyecto está enfocado en **educación** (es un proyecto académico simplificado).
- Usaste **herencia directa** (`BaseEntity`) en lugar de interfaces.
- Esto es válido para un pequeño proyecto; en proyectos grandes, las interfaces permiten más flexibilidad.

**¿Deberías agregar interfaces?**
- Para una defensa: No es necesario. Tu diseño actual (herencia) es válido.
- Si quisieras mejorar el diseño (opcional):
  - Podrías crear `interface Entity { getId(); }` y que `BaseEntity` la implemente.
  - Podrías crear `interface Persistible { save(); load(); }` para Repository.
  - Pero esto es **over-engineering** para un proyecto educativo.

---

## Conclusión: UML recomendado para tu defensa

**Qué incluir:**
- Paquete `model`: BaseEntity, Usuario, Proyecto, Tarea, Etiqueta, FechaLimite, Prioridad, EstadoTarea, RolMiembro.
- Paquete `service`: AuthService, ProjectService, TaskService, ReportService.
- Paquete `storage`: Repository.
- (Opcional) Paquete `export`: ReportData, ReportTable, PdfReportGenerator.

**Qué excluir:**
- UI/Views (ProjectBoardView, LoginView, TaskCard).
- Utilidades (ConsoleUtil, MiembroTableData).
- Main y excepciones simples.

**Relaciones a mostrar:**
- Herencia (extends).
- Composición y agregación (*--, o--).
- Asociación y dependencia (con rótulos).

**Justificación ante el tribunal:**
"El diagrama muestra la arquitectura de dominio y servicios del proyecto. Excluí las vistas JavaFX porque son detalles de presentación; el UML enfatiza la lógica de negocio, las relaciones entre entidades y cómo los servicios orquestan operaciones."

---

## Puntos adicionales para la defensa

- **Separación de capas:** model (dominio) → service (lógica) → storage (datos) → view (presentación). Esto es claramente visible en tu estructura.
- **DTOs (ReportData, ReportTable):** muestran que entiende la necesidad de transportar datos sin acoplar presentación al dominio.
- **Reflexión en PdfReportGenerator:** demuestra manejo avanzado de dependencias externas.
- **Period-limited metrics:** en ReportService muestra pensamiento de negocio (los reportes deben reflejar solo el período solicitado).

---

## Archivos de referencia
- `docs/technical_integration.md` — flujo detallado de cada clase.
- `docs/model_spec.md` — especificación del modelo (si lo actualizas).
- Código fuente comentado (ReportData, ReportTable, PdfReportGenerator, ReportService).

