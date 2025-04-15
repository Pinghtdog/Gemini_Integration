package com.pinghtdog.geminiintegration;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Construct the path to the FXML file relative to the MainApp class
        URL fxmlLocation = getClass().getResource("main-view.fxml");
        if (fxmlLocation == null) {
            System.err.println("Cannot find FXML file. Check the path.");
            return;
        }
        System.out.println("Loading FXML from: " + fxmlLocation); // Debug print

        // Load the FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        // Create and show the scene
        Scene scene = new Scene(root, 600, 500); // Set initial size
        stage.setTitle("Gemini API Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}