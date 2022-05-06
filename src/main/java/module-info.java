module com.example.gamelauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.gamelauncher to javafx.fxml;
    exports com.example.networkdemo;
    opens com.example.networkdemo to javafx.fxml;
}