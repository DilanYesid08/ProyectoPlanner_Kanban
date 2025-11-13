# Guía técnica: interacción práctica entre clases

## `BaseEntity`
Propósito: proveer un identificador único para entidades que lo requieran.

- Campo relevante
  - `protected final String id = UUID.randomUUID().toString();`
    - Efecto: cada instancia recibe un UUID único al crearse.
  - `public String getId()`
    - Uso: otras clases (por ejemplo, `Proyecto` y `Usuario`) exponen su `id` para referencias y almacenamiento (p. ej. `usuario.projectIds` guarda ids de proyectos).

Impacto en otras clases:
- Permite referenciar entidades por id en lugar de por `toString()` o por objeto directo, lo que facilita la limpieza de relaciones (por ejemplo al eliminar un proyecto).

---

## `Usuario` (model)
Propósito: representar un usuario de la aplicación.

Campos importantes:
- `String nombre`, `String email` — datos básicos.
- `List<String> projectIds` — almacena ids (UUID) de proyectos a los que pertenece el usuario.

Métodos clave e interacción:
- `public void agregarProyecto(String projectId)`
  - Qué hace: añade el `projectId` a `projectIds` si no está ya presente.
  - Condiciones: comprueba null y existencia previa para evitar duplicados.
  - Efectos secundarios: mantiene la referencia del usuario a un `Proyecto` por id; no actualiza el objeto `Proyecto` directamente.
  - Usos: invocado por `ProjectService.createProject(...)` y `ProjectService.addMember(...)` para mantener la relación a nivel de usuario.

- `public void removerProyecto(String projectId)`
  - Qué hace: elimina el id del listado si existe.
  - Usos: llamado al borrar un proyecto (`ProjectService.removeProject(...)`) para eliminar referencias huérfanas.

Notas prácticas:
- Guardar `projectIds` como `String` (ids) facilita remover referencias cuando el `Proyecto` se borra del repositorio; sin embargo, cuando se necesita el objeto `Proyecto` hay que buscarlo en el `Repository` por id.

---

### Cambios recientes en `Tarea`
- `fechaCreacion : LocalDateTime` — ahora cada `Tarea` registra la fecha y hora de creación en su constructor; se usa para filtrar métricas por periodo en los reportes.
- `fechaCierre : LocalDateTime` — al cambiar el estado a `COMPLETADA` se registra la fecha de cierre; se usa para contabilizar tareas completadas dentro de un periodo.
- Impacto: estas fechas permiten que `ReportService` construya métricas period-limited (p. ej. tareas creadas/completadas en un rango). Si no hay tareas en el periodo, los KPIs devuelven 0 o valores neutros para evitar divisiones por cero.


## `Proyecto` (model)
Propósito: contener tareas y miembros; semántica de composición sobre `Tarea`.

Campos importantes:
- `String nombre`, `String descripcion`
- `List<Tarea> tareas` — lista mutable de tareas del proyecto (composición).
- `List<Usuario> miembros` — lista de usuarios que participan (agregación/relación).

Métodos clave e interacción:
- `public void agregarMiembro(Usuario usuario)`
  - Qué hace: si `usuario` no es `null` y no está en `miembros`, lo añade.
  - Efectos: añade la relación en el `Proyecto` (lado del proyecto). Normalmente, después de esto, `ProjectService` también añade el `projectId` al `Usuario` (lado del usuario), manteniendo coherencia bidireccional.
  - Condiciones: evita duplicados usando `miembros.contains(usuario)`.

- `public void agregarTarea(Tarea tarea)`
  - Ejemplo:

```java
public void agregarTarea(Tarea tarea) {
    if (tarea != null) tareas.add(tarea);
}
```

  - Qué hace: comprobación nula simple y añade la tarea a la lista `tareas`.
  - Semántica: composición — el proyecto "posee" sus tareas; al eliminar el proyecto se espera que las tareas dejen de estar accesibles (se manejan como parte del ciclo de vida del proyecto).
  - Efectos en otras capas: `TaskService.createTask(...)` crea la `Tarea` y luego llama a este método para anexarla al `Proyecto`.

- `public void mostrarTareas()`
  - Qué hace: itera `tareas` y hace `System.out.println(t)`; útil para la UI de consola (`KanbanApp`).

- `public String toString()`
  - Qué hace: incluye `getId()` en la representación textual; esto ayuda a mostrar ids en el UI y a identificar proyectos cuando se solicita eliminar o seleccionar.

Notas prácticas:
- No se clonan las listas al exponer `getTareas()` y `getMiembros()`: los servicios que modifiquen estas listas trabajan sobre referencias directas.

---

## `Tarea` (model)
Propósito: representar una tarea con estado, prioridad, asignado, etiquetas y fecha límite.

Campos típicos:
- `String titulo`, `String descripcion`
- `Usuario asignadoA` — referencia al usuario (puede ser `null`).
- `FechaLimite fechaLimite` — objeto valor que sabe si está vencido.
- `Prioridad prioridad` — enum.
- `EstadoTarea estado` — enum con etapas (p. ej. TO_DO, IN_PROGRESS, DONE).
- `List<Etiqueta> etiquetas`

Métodos clave e interacción:
- `public void asignarUsuario(Usuario usuario)`
  - Qué hace: asigna la referencia `asignadoA = usuario` si `usuario != null`.
  - Efecto: relaciona la tarea con el objeto `Usuario` en memoria; no modifica `usuario.projectIds` ni el `Usuario` en sí (es unilateral).
  - Uso: `TaskService.createTask(...)` o la UI llaman a este método para asignar responsables.

- `public void agregarEtiqueta(Etiqueta etiqueta)`
  - Qué hace: añade la etiqueta si no es `null` y si no existe ya en la lista (evita duplicados por igualdad).
  - Interacción: las `Etiquetas` son objetos valor; el `Tarea` las mantiene localmente.

- `public void cambiarEstado(EstadoTarea nuevo)`
  - Qué hace: establece `estado = nuevo` después de validaciones mínimas (por ejemplo `nuevo != null`).
  - Efecto: puede cambiar el resultado de `getProgressPercent()` y las representaciones en la UI.
  - Uso: `TaskService.moveToEstado(...)` llama a este método para actualizar el estado.

- `public double getProgressPercent()`
  - Qué hace: calcula un porcentaje simple basado en `estado` (por ejemplo, TO_DO=0, IN_PROGRESS=50, DONE=100).
  - Uso: presentación en UI y métricas rápidas.

Notas prácticas:
- `Tarea` mantiene referencias directas a `Usuario` y `FechaLimite`. Si el `Usuario` se elimina del repositorio, la `Tarea` seguirá apuntando al objeto en memoria hasta que otro proceso lo actualice.

---

## `Etiqueta` (model)
Propósito: representar una etiqueta/laber con nombre y color.

- `equals`/`hashCode` bien definidos (por `nombre` + `color`) permiten evitar etiquetas duplicadas en `Tarea`.
- Uso: gestionadas por `Tarea.agregarEtiqueta(...)` o por el `TaskService`.

---

## `FechaLimite` (model)
Propósito: envolver `LocalDate` con lógica de utilidad.

- `public boolean estaVencida()`
  - Qué hace: compara `fecha.isBefore(LocalDate.now())` o `!fecha.isAfter(now)` y devuelve si la fecha límite ya pasó.
  - Uso: puede usarse por la UI o por un proceso que marque tareas como atrasadas.

---

## Enums: `Prioridad` y `EstadoTarea`
Propósito: valores discretos para priorización y flujo de estado.

- `Prioridad` (LOW, MEDIUM, HIGH): usado en la UI para definir orden o color.
- `EstadoTarea` (ej. TO_DO, IN_PROGRESS, DONE): usado por `Tarea` y `TaskService` para controlar progreso. Sus valores son mapeados en `getProgressPercent()`.

---

### RolMiembro (enum)
- Valores: ADMINISTRADOR, EDITOR, VISUALIZADOR
- Uso: representa el rol de un miembro dentro de un `Proyecto`. Está modelado como enum en `com.proyecto.kanban.model` y ofrece una representación legible (displayName) vía `toString()`.


## `Repository` (storage)
Propósito: almacenamiento en memoria (listas) de `Usuario` y `Proyecto`.

Operaciones típicas e interacción:
- `addUsuario(Usuario u)`, `getUsuarioByEmail(String email)`
  - Qué hace: guardan y recuperan usuarios en memoria.
- `addProyecto(Proyecto p)`, `removeProyectoById(String id)`, `findProjectById(String id)`
  - Qué hace: manipulan proyectos en la colección en memoria.

Notas prácticas importantes:
- El `Repository` devuelve referencias directas a objetos mutables. Los servicios que obtienen instancias desde el repositorio trabajan sobre las mismas instancias en memoria.
- No hay persistencia: reiniciar la aplicación pierde todos los datos.

---

## `AuthService` (service)
Propósito: lógica simple de autenticación/registro.

- `signup(String nombre, String email)`
  - Qué hace: crea un `Usuario` nuevo y lo añade al `Repository` si no existe otro con el mismo email.
  - Efectos: actualiza el estado global en `Repository`.

- `login(String email)`
  - Qué hace: busca por email en el `Repository` y devuelve la instancia si existe.

Interacción con UI y servicios:
- `KanbanApp` usa `AuthService` para crear la sesión de usuario y luego pasa la referencia de `Usuario` a `ProjectService` y `TaskService`.

---

## `ProjectService` (service)
Propósito: operaciones de negocio sobre `Proyecto`.

Métodos clave e interacción:
- `createProject(String nombre, String descripcion, Usuario owner)`
  - Qué hace (pasos típicos):
    1. Crear `Proyecto p = new Proyecto(nombre, descripcion)`.
    2. Añadir `owner` a `p.agregarMiembro(owner)`.
    3. Añadir `p` al `Repository` con `repository.addProyecto(p)`.
    4. Registrar la relación en el `Usuario` con `owner.agregarProyecto(p.getId())` (esto guarda el id en `usuario.projectIds`).
  - Efectos: crea el proyecto y enlaza la relación desde ambos lados (objeto `Proyecto` -> `miembros`, y `Usuario` -> `projectIds`).

- `addMember(Proyecto p, Usuario u)`
  - Qué hace: llama `p.agregarMiembro(u)` y luego `u.agregarProyecto(p.getId())` para mantener coherencia.

- `removeProject(String projectId)`
  - Qué hace (pasos típicos):
    1. Buscar proyecto por id en `Repository`.
    2. Si existe, removerlo del repositorio (`repository.removeProyectoById(projectId)`).
    3. Iterar todos los `Usuario` registrados y llamar `usuario.removerProyecto(projectId)` para limpiar referencias.
  - Efectos secundarios: garantiza que no queden `projectIds` huérfanos en usuarios.

Notas prácticas:
- Importante: `removeProject` no puede restaurar objetos `Tarea` que otros componentes mantengan en memoria. Se asume que el ciclo de vida de las `Tarea` está ligado al `Proyecto` y que la UI ya no podrá acceder a ellas si el proyecto se elimina del `Repository`.

---

## `TaskService` (service)
Propósito: gestionar la creación y actualización de tareas.

Métodos clave e interacción:
- `createTask(Proyecto p, String titulo, String descripcion, Usuario asignado, FechaLimite fl, Prioridad prio)`
  - Qué hace (pasos típicos):
    1. Instanciar `Tarea t = new Tarea(titulo, descripcion, asignado, fl, prio)`.
    2. Si `asignado != null`, `t.asignarUsuario(asignado)`.
    3. Llamar `p.agregarTarea(t)` para anexarla al `Proyecto`.
    4. Opcionalmente devolver `t` o actualizar el `Repository` si el proyecto ya está en él.
  - Efectos: crea la tarea en memoria y la añade al proyecto; no persiste por fuera del `Repository` en memoria.

- `addEtiqueta(Tarea t, Etiqueta e)`
  - Qué hace: delega en `t.agregarEtiqueta(e)` para evitar duplicación de lógica en la UI.

- `moveToEstado(Tarea t, EstadoTarea nuevo)`
  - Qué hace: valida `nuevo` y llama a `t.cambiarEstado(nuevo)`; puede servir para añadir auditoría o checks adicionales.

Notas prácticas:
- `TaskService` actúa como fachada que mantiene invariantes y pasos compuestos (crear + anexar), evitando que la UI tenga que conocer detalles de cómo anexar la tarea al `Proyecto`.

---

## Exportación y reportes (nuevo)
Propósito: generar resúmenes (semanal/mensual) y exportarlos a PDF para auditoría o seguimiento.

- `ReportService` (package `com.proyecto.kanban.service`): construye un `ReportData` con métricas y tablas para un `Proyecto` y un rango de fechas (`desde`/`hasta`). Calcula: tareas creadas, completadas, en progreso, pendientes, vencidas y un `Progreso %` que actualmente se calcula sobre las tareas creadas dentro del periodo seleccionado.

- `ReportData` y `ReportTable` (package `com.proyecto.kanban.export`): DTOs usados para transportar la información del reporte a la capa de presentación/export.
  - `ReportData`: title:String, metadata:Map<String,String>, tables:List<ReportTable>
  - `ReportTable`: name:String, columns:List<String>, rows:List<List<String>>

- `PdfReportGenerator` (package `com.proyecto.kanban.export`): genera un XHTML básico a partir de `ReportData` y lo renderiza a PDF.
  - Detalle: para evitar fricciones con `module-info.java` y dependencias de tiempo de ejecución, la integración con OpenHTMLToPDF se hace vía reflexión. Las excepciones reflectivas se desempaquetan para mostrar la causa raíz al usuario.

- Ruta de export: los PDFs se guardan por defecto en la carpeta `Informes` dentro del directorio de trabajo de la aplicación (`user.dir/Informes`). La UI notifica la ruta completa del PDF al usuario después de exportar.

- Dependencia técnica: OpenHTMLToPDF (`com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10`) se usa para convertir HTML/XHTML a PDF. Requiere estar presente en el classpath en tiempo de ejecución; PdfReportGenerator lanza una excepción clara si no se encuentra.

- Consecuencia para el diseño: `ReportData`/`ReportTable` se colocaron en `export` (no en `model`) para dejar claro que son artefactos de presentación/exportación y no parte del dominio.


## `ConsoleUtil` (util)
Propósito: facilitar entrada/salida por consola (lectura segura, confirmaciones, impresión formateada).

- Uso: `KanbanApp` usa sus helpers para leer cadenas, números y confirmar operaciones (por ejemplo, preguntar antes de eliminar un proyecto).
- Impacto: simplifica el código en `KanbanApp` y centraliza el comportamiento de la CLI (p. ej. validaciones de entrada).

---

## `KanbanApp` (main)
Propósito: runner de consola que orquesta `AuthService`, `ProjectService`, `TaskService` y `Repository`.

Comportamiento relevante:
- Flujos: signup/login -> lista/creación de proyectos -> menú de proyecto -> creación/edición de tareas.
- Selección de proyecto: la UI muestra `Proyecto.toString()` (incluye id) para que el usuario identifique el proyecto a operar.
- Eliminación de proyecto: ejecuta `ProjectService.removeProject(projectId)` y muestra resultados al usuario.

Notas prácticas:
- `KanbanApp` debe tratar referencias devueltas por `Repository` como objetos vivos; cualquier cambio en ellos se refleja en el estado compartido.

---

## Ejemplos mínimos y explicaciones rápidas

- `Proyecto.agregarTarea(Tarea tarea)`
  - Verifica null y añade: evita NullPointerException y mantiene invariante de que la lista solo contiene instancias válidas.

- `Usuario.agregarProyecto(String projectId)`
  - Verifica duplicados antes de añadir: evita inserciones repetidas y hace que `projectIds` funcione como set (implícitamente).

- `ProjectService.createProject(...)` (resumen de flujo)
  - Crea `Proyecto`, añade `owner` a `miembros`, guarda el proyecto en `Repository`, y registra `projectId` en `owner.projectIds`.
  - Resultado: dos estructuras (objeto `Proyecto` y `Usuario.projectIds`) apuntan a la misma relación desde distintos ángulos.

---

## Riesgos y recomendaciones prácticas

- Actualmente las colecciones se exponen directamente (p. ej. `getTareas()` devuelve la lista interna). Recomiendo considerar devolver copias inmutables o `Collections.unmodifiableList()` si se desea evitar modificaciones externas inesperadas.
- Si se añade persistencia real en el futuro, mantener `projectIds` como ids es una buena práctica para relaciones ligeras; para consultas frecuentes se puede mantener cachés o índices en el repositorio.
- Cuando se asigna un `Usuario` a una `Tarea`, la relación es unidireccional en el modelo actual (la `Tarea` apunta a `Usuario`). Si necesitas ver "todas las tareas asignadas a un usuario" implementa un método que haga un escaneo de proyectos/tareas o una estructura inversa en `Usuario` (p. ej. `List<String> assignedTaskIds`).

---

## Conclusión
Este documento resume cómo actúan las clases principales y sus métodos cuando interactúan. Está pensado para el mantenimiento y para enseñar a estudiantes la semántica de cada operación: comprobaciones (null, duplicados), efectos secundarios (añadir a listas, actualizar ids en `Usuario`), y responsabilidad de los servicios de orquestar pasos compuestos (crear + asociar).

Si quieres, puedo:
- Añadir diagramas de secuencia simplificados para `createProject` y `createTask`.
- Generar tests unitarios mínimos (happy path) que verifiquen estos flujos.

---
