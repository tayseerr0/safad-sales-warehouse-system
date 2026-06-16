package ui.fx;

import db.DBConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;

public class FxMainApp extends Application {

    @Override
    public void start(Stage stage) {
        boolean connected = DBConnection.testConnection();

        MainLayout mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout.getRoot(), 1280, 760);

        URL css = FxMainApp.class.getResource("styles/safad.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("SAFAD Sales and Warehouse Management System - JavaFX");
        stage.setMinWidth(1080);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();

        if (!connected) {
            FxTheme.showAlert(
                    Alert.AlertType.WARNING,
                    "Database Connection",
                    "Failed to connect to safad_db. Check MySQL server, database name, username, and password."
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
