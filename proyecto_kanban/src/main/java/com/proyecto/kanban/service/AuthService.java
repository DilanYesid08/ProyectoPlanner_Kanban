package com.proyecto.kanban.service;

import com.proyecto.kanban.model.Usuario;
import com.proyecto.kanban.storage.Repository;
// Persistencia eliminada: el repositorio ahora vive solo en memoria

import java.util.Optional;

/**
 * Servicio mínimo de autenticación.
 *
 * Implementa operaciones básicas:
 * - signup: crea un usuario si el email no existe
 * - login: busca usuario por email
 *
 * NOTA: Este servicio es intencionalmente simple (sin password). Es suficiente
 * para la simulación por consola; en un proyecto real habría hashing y almacenamiento seguro.
 */
public class AuthService {
    private final Repository repo;

    public AuthService(Repository repo) {
        this.repo = repo;
    }

    /**
     * Registra un nuevo usuario si el email no está en uso.
     * @return Usuario creado o null si ya existe el email.
     */
    public Usuario signup(String nombre, String email) {
        // Simple: verificar que no exista email
        Optional<Usuario> exists = repo.getUsuarios().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
        if (exists.isPresent()) return null;
        Usuario u = new Usuario(nombre, email);
        repo.getUsuarios().add(u);
        return u;
    }

    /** 
     * Busca un usuario por email. Si no existe, lo crea automáticamente.
     * Esto es válido para nuestra simulación donde no necesitamos autenticación real.
     */
    public Usuario login(String email) {
        return repo.getUsuarios().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseGet(() -> {
                    // Si no existe, creamos un usuario automáticamente
                    String nombre = email.split("@")[0]; // Usamos la parte antes del @ como nombre
                    Usuario nuevoUsuario = new Usuario(nombre, email);
                    repo.getUsuarios().add(nuevoUsuario);
                    return nuevoUsuario;
                });
    }

    /**
     * Devuelve el repositorio subyacente. Se expone para permitir que
     * componentes de la UI o servicios reutilicen el mismo almacenamiento en memoria.
     */
    public com.proyecto.kanban.storage.Repository getRepo() {
        return repo;
    }
}
