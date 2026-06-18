package ui.fx;

import db.DBConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.AppUser;

import java.net.URL;

public class FxMainApp extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        boolean connected = DBConnection.testConnection();

        showLogin();
        stage.setMinWidth(1080);
        stage.setMinHeight(680);
        stage.show();

        if (!connected) {
            FxTheme.showAlert(
                    Alert.AlertType.WARNING,
                    "Database Connection",
                    "Failed to connect to safad_db. Check MySQL server, database name, username, and password."
            );
        }
    }

    private void showLogin() {
        LoginFxPage loginPage = new LoginFxPage(this::showMainApp);
        Scene scene = createScene(loginPage, 1080, 680);
        stage.setTitle("SAFAD Login");
        stage.setScene(scene);
    }

    private void showMainApp(AppUser user) {
        SessionManager.login(user);
        MainLayout mainLayout = new MainLayout(this::showLogin);
        Scene scene = createScene(mainLayout.getRoot(), 1280, 760);
        stage.setTitle("SAFAD Sales and Warehouse Management System - JavaFX");
        stage.setScene(scene);
    }

    private Scene createScene(javafx.scene.Parent root, int width, int height) {
        Scene scene = new Scene(root, width, height);

        URL css = FxMainApp.class.getResource("styles/safad.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
