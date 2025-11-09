package com.proyecto.kanban.model;

public enum RolMiembro {
    ADMINISTRADOR("Administrador"),
    EDITOR("Editor"),
    VISUALIZADOR("Visualizador");

    private final String displayName;

    RolMiembro(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}