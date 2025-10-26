package com.proyecto.kanban.storage;

import com.proyecto.kanban.model.Proyecto;
import com.proyecto.kanban.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio en memoria sencillo.
 *
 * Solo mantiene listas en memoria de usuarios y proyectos. No realiza
 * persistencia en disco (esto simplifica el proyecto para principiantes).
 */
public class Repository {

    private List<Usuario> usuarios;
    private List<Proyecto> proyectos;

    public Repository() {
        this.usuarios = new ArrayList<>();
        this.proyectos = new ArrayList<>();
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }
    public List<Proyecto> getProyectos() { return proyectos; }
    public void setProyectos(List<Proyecto> proyectos) { this.proyectos = proyectos; }
}
