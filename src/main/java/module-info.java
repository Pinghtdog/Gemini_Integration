module com.pinghtdog.geminiintegration {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.net.http;
    requires com.google.gson;


    opens com.pinghtdog.geminiintegration to javafx.fxml;
    exports com.pinghtdog.geminiintegration;
}