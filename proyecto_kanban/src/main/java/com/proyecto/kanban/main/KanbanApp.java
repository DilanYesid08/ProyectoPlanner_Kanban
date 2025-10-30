package com.proyecto.kanban.main;

import com.proyecto.kanban.exceptions.FechaInvalidaException;
import com.proyecto.kanban.model.FechaLimite;
import com.proyecto.kanban.model.Prioridad;
import com.proyecto.kanban.model.Proyecto;
import com.proyecto.kanban.model.Tarea;
import com.proyecto.kanban.model.Usuario;
import com.proyecto.kanban.model.EstadoTarea;
import com.proyecto.kanban.service.AuthService;
import com.proyecto.kanban.service.ProjectService;
import com.proyecto.kanban.service.TaskService;
import com.proyecto.kanban.storage.Repository;
import com.proyecto.kanban.util.ConsoleUtil;

import java.util.List;

/**
 * Aplicación de consola básica para gestionar proyectos Kanban.
 *
 * Flujo mínimo implementado:
 * - Registro (signup) e inicio de sesión por email (no hay password, es una simulación)
 * - Crear proyectos, listar proyectos
 * - Crear tareas con fecha límite y prioridad y añadirlas a proyectos
 *
 * Las responsabilidades están distribuidas en pequeños servicios (AuthService, ProjectService,
 * TaskService) para demostrar separación de responsabilidades y facilitar mejoras futuras.
 */
public class KanbanApp {
    public static void main(String[] args) {
    // Repositorio en memoria (simplificado para aprendizaje)
    Repository repo = new Repository();
        AuthService auth = new AuthService(repo);
        ProjectService pService = new ProjectService(repo);
        TaskService tService = new TaskService();

        while (true) {
            System.out.println("=== Kanban Console - Bienvenido ===");
            System.out.println("1) Registrarse");
            System.out.println("2) Iniciar sesion");
            System.out.println("0) Salir");
            int opt = ConsoleUtil.readInt("Seleccione una opcion");
            if (opt == 0) {
                System.out.println("Saliendo...");
                break;
            }
            if (opt == 1) {
                String nombre = ConsoleUtil.readLine("Nombre");
                String email = ConsoleUtil.readLine("Email");
                Usuario u = auth.signup(nombre, email);
                if (u == null) System.out.println("Email ya registrado.");
                else System.out.println("Usuario creado: " + u.getNombre());
                ConsoleUtil.pause();
                continue;
            }
            if (opt == 2) {
                String email = ConsoleUtil.readLine("Email");
                Usuario user = auth.login(email);
                if (user == null) {
                    System.out.println("Usuario no encontrado.");
                    ConsoleUtil.pause();
                    continue;
                }
                userMenu(user, pService, tService);
            }
        }
    }

    private static void userMenu(Usuario user, ProjectService pService, TaskService tService) {
        while (true) {
            System.out.println("\n--- Usuario: " + user.getNombre() + " ---");
            System.out.println("1) Crear proyecto");
            System.out.println("2) Ver y gestionar proyectos");
            System.out.println("3) Crear tarea en proyecto");
            System.out.println("0) Cerrar sesion");
            int opt = ConsoleUtil.readInt("Seleccione una opcion");
            if (opt == 0) break;

            if (opt == 1) {
                String nombre = ConsoleUtil.readLine("Nombre del proyecto");
                String desc = ConsoleUtil.readLine("Descripcion");
                Proyecto p = pService.createProject(nombre, desc, user);
                System.out.println("Proyecto creado: " + p.getNombre());
                ConsoleUtil.pause();
                continue;
            }

            if (opt == 2) {
                List<Proyecto> list = pService.getAll();
                if (list.isEmpty()) {
                    System.out.println("No hay proyectos.");
                    ConsoleUtil.pause();
                    continue;
                }
                for (int i = 0; i < list.size(); i++) System.out.println((i+1) + ") " + list.get(i).getNombre());
                int sel = ConsoleUtil.readInt("Seleccione proyecto para gestionar (numero) o 0 para volver") - 1;
                if (sel == -1) continue;
                if (sel < 0 || sel >= list.size()) {
                    System.out.println("Seleccion invalida.");
                    ConsoleUtil.pause();
                    continue;
                }
                Proyecto target = list.get(sel);
                projectMenu(target, user, pService, tService);
                continue;
            }

            if (opt == 3) {
                List<Proyecto> list = pService.getAll();
                if (list.isEmpty()) {
                    System.out.println("No hay proyectos.");
                    ConsoleUtil.pause();
                    continue;
                }
                for (int i = 0; i < list.size(); i++) System.out.println((i+1) + ") " + list.get(i).getNombre());
                int sel = ConsoleUtil.readInt("Seleccione proyecto (numero)") - 1;
                    if (sel < 0 || sel >= list.size()) {
                    System.out.println("Seleccion invalida.");
                    ConsoleUtil.pause();
                    continue;
                }
                Proyecto target = list.get(sel);
                createTaskInteractive(target, user, pService, tService);
            }
        }
    }

    private static void projectMenu(Proyecto project, Usuario user, ProjectService pService, TaskService tService) {
        while (true) {
            System.out.println("\n--- Proyecto: " + project.getNombre() + " ---");
            System.out.println("ID: " + project.getId());
            System.out.println("1) Mostrar tareas (con progreso)");
            System.out.println("2) Crear tarea en este proyecto");
            System.out.println("3) Agregar miembro al proyecto");
            System.out.println("6) Eliminar proyecto");
            System.out.println("4) Filtrar tareas");
            System.out.println("5) Gestionar tarea (seleccionar tarea)");
            System.out.println("0) Volver");
            int opt = ConsoleUtil.readInt("Seleccione una opcion");
            if (opt == 0) break;
            if (opt == 1) {
                showTasksWithProgress(project);
                ConsoleUtil.pause();
                continue;
            }
            if (opt == 2) {
                createTaskInteractive(project, user, pService, tService);
                continue;
            }
            if (opt == 3) {
                String nombre = ConsoleUtil.readLine("Nombre del miembro");
                String email = ConsoleUtil.readLine("Email del miembro");
                // Para simplificar no buscamos en un repo global; creamos el usuario y lo añadimos.
                Usuario u = new Usuario(nombre, email);
                pService.addMember(project, u);
                System.out.println("Miembro añadido: " + u.getNombre());
                ConsoleUtil.pause();
                continue;
            }
            if (opt == 6) {
                String confirm = ConsoleUtil.readLine("Seguro que desea eliminar este proyecto? (s/n)");
                if ("s".equalsIgnoreCase(confirm)) {
                    pService.removeProject(project);
                    System.out.println("Proyecto eliminado.");
                    ConsoleUtil.pause();
                    break;
                } else {
                    System.out.println("Eliminacion cancelada.");
                    ConsoleUtil.pause();
                    continue;
                }
            }
            if (opt == 4) {
                filterTasksInteractive(project);
                continue;
            }
            if (opt == 5) {
                if (project.getTareas().isEmpty()) {
                    System.out.println("El proyecto no tiene tareas.");
                    ConsoleUtil.pause();
                    continue;
                }
                for (int i = 0; i < project.getTareas().size(); i++) {
                    Tarea t = project.getTareas().get(i);
                    System.out.println((i+1) + ") " + t.getTitulo() + " - " + t.getEstado() + " (" + t.getProgressPercent() + "%)");
                }
                int sel = ConsoleUtil.readInt("Seleccione tarea (número) o 0 para volver") - 1;
                if (sel == -1) continue;
                if (sel < 0 || sel >= project.getTareas().size()) {
                    System.out.println("Selección inválida.");
                    ConsoleUtil.pause();
                    continue;
                }
                taskMenu(project.getTareas().get(sel), project);
                continue;
            }
        }
    }

    private static void showTasksWithProgress(Proyecto project) {
        if (project.getTareas().isEmpty()) {
            System.out.println("No hay tareas en este proyecto.");
            return;
        }
        for (Tarea t : project.getTareas()) {
            String asignado = t.getAsignadoA() != null ? t.getAsignadoA().getNombre() : "Sin asignar";
            System.out.println("- " + t.getTitulo() + " | " + t.getEstado() + " | " + t.getProgressPercent() + "% | Asignado: " + asignado + " | Prioridad: " + t.getPrioridad());
        }
    }

    private static void createTaskInteractive(Proyecto project, Usuario user, ProjectService pService, TaskService tService) {
    String titulo = ConsoleUtil.readLine("Titulo de la tarea");
    String desc = ConsoleUtil.readLine("Descripcion");
    
    FechaLimite fecha = null;
    while (fecha == null) {
        try {
            int dia = ConsoleUtil.readInt("Día de fecha límite (num)");
            int mes = ConsoleUtil.readInt("Mes de fecha límite (num)");
            int año = ConsoleUtil.readInt("Año de fecha límite (num, entre 2000-2100)");
            // Primero validamos
            FechaLimite.validarFecha(dia, mes, año);
            // Si pasa la validación, creamos la fecha
            fecha = new FechaLimite(dia, mes, año);
        } catch (FechaInvalidaException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Por favor, ingrese la fecha nuevamente.");
        }
    }
        System.out.println("Prioridades: 1-URGENTE 2-IMPORTANTE 3-MEDIA 4-BAJA");
    int p = ConsoleUtil.readInt("Seleccione prioridad (numero)");
        Prioridad prioridad = switch (p) {
            case 1 -> Prioridad.URGENTE;
            case 2 -> Prioridad.IMPORTANTE;
            case 3 -> Prioridad.MEDIA;
            default -> Prioridad.BAJA;
        };
        Tarea tarea = tService.createTask(titulo, desc, user, fecha, prioridad, EstadoTarea.PENDIENTE);
        pService.addTask(project, tarea);
        System.out.println("Tarea creada y añadida al proyecto.");
        ConsoleUtil.pause();
    }

    private static void filterTasksInteractive(Proyecto project) {
    System.out.println("Filtrar por: 1) Prioridad 2) Estado 3) Asignado 4) Etiqueta 0) Volver");
    int opt = ConsoleUtil.readInt("Opcion");
        if (opt == 0) return;
        switch (opt) {
            case 1 -> {
                System.out.println("Prioridades: 1-URGENTE 2-IMPORTANTE 3-MEDIA 4-BAJA");
                int p = ConsoleUtil.readInt("Seleccione prioridad (numero)");
                Prioridad prioridad = switch (p) {
                    case 1 -> Prioridad.URGENTE;
                    case 2 -> Prioridad.IMPORTANTE;
                    case 3 -> Prioridad.MEDIA;
                    default -> Prioridad.BAJA;
                };
                project.getTareas().stream().filter(t -> t.getPrioridad() == prioridad).forEach(t -> System.out.println(t));
                ConsoleUtil.pause();
            }
            case 2 -> {
                System.out.println("Estados: 1-PENDIENTE 2-EN_PROGRESO 3-COMPLETADA");
                int e = ConsoleUtil.readInt("Seleccione estado (numero)");
                EstadoTarea estado = switch (e) {
                    case 1 -> EstadoTarea.PENDIENTE;
                    case 2 -> EstadoTarea.EN_PROGRESO;
                    default -> EstadoTarea.COMPLETADA;
                };
                project.getTareas().stream().filter(t -> t.getEstado() == estado).forEach(t -> System.out.println(t));
                ConsoleUtil.pause();
            }
            case 3 -> {
                String nombre = ConsoleUtil.readLine("Nombre del asignado (exacto)");
                project.getTareas().stream().filter(t -> t.getAsignadoA() != null && t.getAsignadoA().getNombre().equalsIgnoreCase(nombre)).forEach(t -> System.out.println(t));
                ConsoleUtil.pause();
            }
            case 4 -> {
                String tag = ConsoleUtil.readLine("Nombre de la etiqueta");
                project.getTareas().stream().filter(t -> t.getEtiquetas().stream().anyMatch(et -> et.getNombre().equalsIgnoreCase(tag))).forEach(t -> System.out.println(t));
                ConsoleUtil.pause();
            }
            default -> System.out.println("Opcion invalida");
        }
    }

    private static void taskMenu(Tarea tarea, Proyecto proyecto) {
        while (true) {
            System.out.println("\n--- Tarea: " + tarea.getTitulo() + " ---");
            System.out.println("1) Cambiar estado");
            System.out.println("2) Asignar miembro");
            System.out.println("3) Cambiar prioridad");
            System.out.println("4) Añadir etiqueta");
            System.out.println("0) Volver");
            int opt = ConsoleUtil.readInt("Seleccione una opción");
            if (opt == 0) break;
            if (opt == 1) {
                System.out.println("Estados: 1-PENDIENTE 2-EN_PROGRESO 3-COMPLETADA");
                int e = ConsoleUtil.readInt("Seleccione estado (numero)");
                EstadoTarea estado = switch (e) {
                    case 1 -> EstadoTarea.PENDIENTE;
                    case 2 -> EstadoTarea.EN_PROGRESO;
                    default -> EstadoTarea.COMPLETADA;
                };
                tarea.cambiarEstado(estado);
                System.out.println("Estado cambiado.");
                ConsoleUtil.pause();
                continue;
            }
            if (opt == 2) {
                if (proyecto.getMiembros().isEmpty()) {
                    System.out.println("No hay miembros en el proyecto.");
                    ConsoleUtil.pause();
                    continue;
                }
                for (int i = 0; i < proyecto.getMiembros().size(); i++) System.out.println((i+1) + ") " + proyecto.getMiembros().get(i).getNombre());
                int m = ConsoleUtil.readInt("Seleccione miembro (número)") - 1;
                if (m < 0 || m >= proyecto.getMiembros().size()) {
                    System.out.println("Seleccion invalida.");
                    ConsoleUtil.pause();
                    continue;
                }
                tarea.asignarUsuario(proyecto.getMiembros().get(m));
                System.out.println("Miembro asignado.");
                ConsoleUtil.pause();
                continue;
            }
            if (opt == 3) {
                System.out.println("Prioridades: 1-URGENTE 2-IMPORTANTE 3-MEDIA 4-BAJA");
                int p = ConsoleUtil.readInt("Seleccione prioridad (numero)");
                Prioridad prioridad = switch (p) {
                    case 1 -> Prioridad.URGENTE;
                    case 2 -> Prioridad.IMPORTANTE;
                    case 3 -> Prioridad.MEDIA;
                    default -> Prioridad.BAJA;
                };
                tarea.setPrioridad(prioridad);
                System.out.println("Prioridad actualizada.");
                ConsoleUtil.pause();
                continue;
            }
            if (opt == 4) {
                String tag = ConsoleUtil.readLine("Nombre de la etiqueta");
                String color = ConsoleUtil.readLine("Color de la etiqueta (p.ej. rojo)");
                tarea.agregarEtiqueta(new com.proyecto.kanban.model.Etiqueta(tag, color));
                System.out.println("Etiqueta añadida.");
                ConsoleUtil.pause();
                continue;
            }
        }
    }
}
