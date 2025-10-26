package com.proyecto.kanban.model;

import java.util.UUID;

/**
 * Clase base para entidades que requieren un identificador único.
 * <p>
 * Propósito:
 * - Proveer un campo `id` común a todas las entidades que lo necesiten.
 * - Servir como ejemplo pedagógico de herencia (usando la palabra clave `extends`).
 *
 * Uso:
 * - Las clases que extiendan {@code BaseEntity} heredarán el campo {@code id}
 *   y el método {@link #getId()} sin necesitar declararlo de nuevo.
 * - En el constructor de la subclase se llama a {@code super()} para ejecutar
 *   la inicialización definida aquí (generación del UUID).
 */
public abstract class BaseEntity {
    /** Identificador único generado al crear la instancia. */
    protected final String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
