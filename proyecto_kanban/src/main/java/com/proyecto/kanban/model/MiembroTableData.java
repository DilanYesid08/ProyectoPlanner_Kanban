package com.proyecto.kanban.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * MiembroTableData (DEPRECATED)
 *
 * Esta clase fue usada originalmente para representar filas en la tabla de
 * miembros de la UI. Actualmente la implementación destinada a la vista se
 * encuentra en `com.proyecto.kanban.view.util.MiembroTableData` y es la que
 * utiliza la UI. Conservamos esta versión marcada como {@code @Deprecated}
 * para evitar romper integraciones en caso de que algún código antiguo la
 * referencie. Se recomienda migrar todo uso a la clase en el paquete
 * `view.util` y, una vez validado, eliminar esta clase.
 */
@Deprecated
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