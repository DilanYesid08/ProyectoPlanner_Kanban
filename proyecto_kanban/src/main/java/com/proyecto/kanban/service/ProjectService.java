package com.proyecto.kanban.service;

import com.proyecto.kanban.model.Proyecto;
import com.proyecto.kanban.model.Tarea;
import com.proyecto.kanban.model.Usuario;
import com.proyecto.kanban.storage.Repository;
// Persistencia eliminada: el repositorio ahora vive solo en memoria

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones sobre proyectos.
 *
 * Responsabilidades:
 * - Crear proyectos y añadir miembros
 * - Añadir tareas a proyectos
 * - Listar proyectos
 *
 * Nota: la forma en que se almacenan las referencias a proyectos en Usuario es intencionalmente
 * simple (se usa el resultado de toString()). Esto facilita la simulación y evita introducir IDs
 * y lógica adicional. Para mejoras futuras, conviene usar IDs y buscar proyectos por id.
 */
public class ProjectService {
    private final Repository repo;

    public ProjectService(Repository repo) {
        this.repo = repo;
    }

    /** Crea un proyecto, añade el owner como miembro y persiste el repositorio. */
    public Proyecto createProject(String nombre, String descripcion, Usuario owner) {
        Proyecto p = new Proyecto(nombre, descripcion);
        p.agregarMiembro(owner);
        repo.getProyectos().add(p);
        // Referencia simple en Usuario: se almacena la representación del proyecto.
        owner.agregarProyecto(p.toString());
        return p;
    }

    /** Añade un miembro a un proyecto y persiste. */
    public void addMember(Proyecto proyecto, Usuario usuario) {
        proyecto.agregarMiembro(usuario);
        usuario.agregarProyecto(proyecto.toString());
    }

    /** Añade una tarea a un proyecto y persiste. */
    public void addTask(Proyecto proyecto, Tarea tarea) {
        proyecto.agregarTarea(tarea);
    }

    /** Lista proyectos donde el usuario aparece (método simple para la demo). */
    public List<Proyecto> listProjectsForUser(Usuario usuario) {
        List<Proyecto> list = new ArrayList<>();
        for (Proyecto p : repo.getProyectos()) {
            if (p.toString().contains(usuario.getNombre())) list.add(p);
        }
        return list;
    }

    /** Devuelve todos los proyectos (copia simple de la lista interna). */
    public List<Proyecto> getAll() {
        return repo.getProyectos().stream().collect(Collectors.toList());
    }
}
