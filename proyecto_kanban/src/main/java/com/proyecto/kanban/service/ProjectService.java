package com.proyecto.kanban.service;

import com.proyecto.kanban.model.Proyecto;
import com.proyecto.kanban.model.Tarea;
import com.proyecto.kanban.model.Usuario;
import com.proyecto.kanban.storage.Repository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de proyectos en el sistema Kanban.
 * Maneja la creación, actualización y consulta de proyectos.
 */
public class ProjectService {
    private final Repository repository;

    public ProjectService(Repository repository) {
        this.repository = repository;
    }

    /**
     * Crea un nuevo proyecto con el usuario creador como líder.
     * @param nombre Nombre del proyecto
     * @param descripcion Descripción del proyecto
     * @param lider Usuario que será el líder del proyecto
     * @return El proyecto creado
     */
    public Proyecto crearProyecto(String nombre, String descripcion, Usuario lider) {
        // Usar el constructor con descripción
        Proyecto proyecto = new Proyecto(nombre, descripcion);
        // Agregar el líder como primer miembro
        if (lider != null) {
            proyecto.agregarMiembro(lider);
        }
        repository.getProyectos().add(proyecto);
        return proyecto;
    }

    /**
     * Obtiene todos los proyectos donde el usuario es miembro.
     * @param usuario Usuario del que se quieren obtener los proyectos
     * @return Lista de proyectos donde el usuario es miembro
     */
    public List<Proyecto> getProyectosUsuario(Usuario usuario) {
        return repository.getProyectos().stream()
                .filter(p -> p.getMiembros().contains(usuario))
                .collect(Collectors.toList());
    }

    /**
     * Añade una tarea al proyecto especificado.
     * @param proyecto Proyecto al que se añadirá la tarea
     * @param tarea Tarea a añadir
     */
    public void agregarTarea(Proyecto proyecto, Tarea tarea) {
        if (proyecto != null && tarea != null) {
            proyecto.agregarTarea(tarea);
        }
    }

    /**
     * Añade un miembro al proyecto si no está ya incluido.
     * @param proyecto Proyecto al que se añadirá el miembro
     * @param usuario Usuario a añadir como miembro
     */
    public void agregarMiembro(Proyecto proyecto, Usuario usuario) {
        if (proyecto != null && usuario != null) {
            proyecto.agregarMiembro(usuario);
        }
    }
}
