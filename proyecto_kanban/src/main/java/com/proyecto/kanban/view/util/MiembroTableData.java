package com.proyecto.kanban.view.util;

import com.proyecto.kanban.model.RolMiembro;
import com.proyecto.kanban.model.Usuario;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Clase auxiliar para representar una fila en la tabla de miembros (UI).
 * Está en el paquete de vista para separar la lógica de presentación del dominio.
 */
public class MiembroTableData {
    private final Usuario usuario;
    private final ObjectProperty<RolMiembro> rol;

    public MiembroTableData(Usuario usuario, RolMiembro rol) {
        this.usuario = usuario;
        this.rol = new SimpleObjectProperty<>(rol);
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public RolMiembro getRol() {
        return rol.get();
    }

    public void setRol(RolMiembro rol) {
        this.rol.set(rol);
    }

    public ObjectProperty<RolMiembro> rolProperty() {
        return rol;
    }
}
