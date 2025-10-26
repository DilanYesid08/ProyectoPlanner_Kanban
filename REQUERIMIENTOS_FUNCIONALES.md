# Requerimientos funcionales — Proyecto Kanban (simulación consola)

Listado de requerimientos funcionales que implementa (o deja preparado) este proyecto.

1. Autenticación básica
   - El usuario puede registrarse (signup) indicando nombre y email.
   - El usuario puede iniciar sesión (login) usando su email.

2. Gestión de proyectos
   - Crear proyectos con nombre y descripción.
   - Listar proyectos disponibles.
   - Añadir miembros a un proyecto.

3. Gestión de tareas
   - Crear tareas con título, descripción, fecha límite y prioridad.
   - Asignar tareas a usuarios (miembros del proyecto).
   - Añadir etiquetas a tareas (nombre + color).
   - Cambiar el estado de la tarea (Pendiente, En progreso, Completada).
   - Calcular un progreso simple de la tarea según su estado (0/50/100%).

4. Vista y filtrado
   - Mostrar tareas de un proyecto con su progreso, asignado y prioridad.
   - Filtrar tareas por prioridad, estado, asignado y etiqueta.

5. Interacción por consola
   - Menús interactivos por consola para registrar, iniciar sesión y gestionar proyectos y tareas.
   - Menú específico por proyecto con opciones: mostrar tareas, crear tarea, agregar miembro, filtrar y gestionar tareas individualmente.

6. Diseño y extensibilidad
   - Código organizado por paquetes (model, service, util, storage, main) para facilitar la extensión.
   - Servicios ligeros (AuthService, ProjectService, TaskService) que encapsulan la lógica de negocio básica.

Limitaciones actuales (implementación intencionalmente simple)
- No hay almacenamiento persistente; los datos viven en memoria durante la ejecución.
- No hay validación avanzada (ej. formatos de email, control de usuarios duplicados más allá del email en signup simple).
- No hay control de permisos: cualquier usuario puede gestionar elementos una vez dentro.

Estas limitaciones se pueden resolver como mejoras futuras (persistencia, validaciones, permisos).
