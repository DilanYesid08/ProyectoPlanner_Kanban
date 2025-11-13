module com.proyecto.kanban {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires java.desktop;

    opens com.proyecto.kanban.main to javafx.fxml, javafx.graphics;
    opens com.proyecto.kanban.model to javafx.base;
    opens com.proyecto.kanban.view to javafx.fxml, javafx.graphics;
    opens com.proyecto.kanban.service to javafx.graphics;

    exports com.proyecto.kanban.main;
    exports com.proyecto.kanban.model;
    exports com.proyecto.kanban.view;
    exports com.proyecto.kanban.service;
    exports com.proyecto.kanban.export;
    exports com.proyecto.kanban.storage;
    exports com.proyecto.kanban.view.util;
}