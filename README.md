# ProyectoPlanner_Kanban — Simulador Kanban (consola)

Proyecto de ejemplo en Java que simula las funciones básicas de un gestor Kanban (inspirado en Microsoft Planner) en consola. El objetivo es servir como proyecto didáctico de POO: modelos, servicios y un menú de consola para crear proyectos, tareas, miembros y gestionar el flujo de trabajo.

Estado: versión en memoria (sin persistencia), simple y fácil de entender para estudiantes.

## Estructura principal

- `com.proyecto.kanban.model` — clases de dominio (Usuario, Proyecto, Tarea, Etiqueta, FechaLimite, Prioridad, EstadoTarea).
- `com.proyecto.kanban.service` — servicios (AuthService, ProjectService, TaskService).
- `com.proyecto.kanban.storage` — `Repository` en memoria (listas de usuarios y proyectos).
- `com.proyecto.kanban.util` — utilidades de consola (`ConsoleUtil`).
- `com.proyecto.kanban.main` — clase principal `KanbanApp` con menú interactivo por consola.

## Requisitos para compilar y ejecutar

- JDK 17 instalado y `javac`/`java` en PATH.
- Workspace con la estructura del proyecto (esta carpeta contiene `proyecto_kanban/src/main/java`).

## Compilar y ejecutar (PowerShell)

1) Generar lista de archivos Java y compilar:

```powershell
Get-ChildItem -Recurse proyecto_kanban/src/main/java -Filter *.java | ForEach-Object { $_.FullName } > files.txt
cmd /c "javac -d proyecto_kanban/target/classes @files.txt"
```

2) Ejecutar la aplicación:

```powershell
java -cp proyecto_kanban/target/classes com.proyecto.kanban.main.KanbanApp
```

En Linux/macOS puedes usar comandos equivalentes con `find` en vez de `Get-ChildItem`.

## Uso básico

- Al iniciar verás opciones para registrarte (signup) o iniciar sesión (login) por email.
- Desde el menú de usuario puedes crear proyectos, listar proyectos, crear tareas y gestionar proyectos.
- Al gestionar un proyecto puedes ver tareas con progreso, agregar miembros, filtrar tareas, y seleccionar una tarea para cambiar su estado, prioridad, asignarle un miembro o añadir etiquetas.

## Notas para estudiantes

- El diseño busca claridad: separa modelos (POJOs) y servicios. Comenta las relaciones entre clases en el código.
- No hay persistencia: todo vive en memoria durante la ejecución (opción intencional para simplificar).
- Si quieres persistencia en el futuro, podemos añadir lectura/escritura JSON o serialización como opción extra.

## Siguientes mejoras sugeridas

- Añadir persistencia opcional (JSON/archivo)
- Exportar plan/tareas a CSV
- Añadir pruebas unitarias (Junit)
- Mejorar búsqueda/filtrado y orden por prioridad/fecha

(Todo esto por ahora)