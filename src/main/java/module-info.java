module com.example.sortx {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens com.example.sortx to javafx.fxml;
    exports com.example.sortx;
}