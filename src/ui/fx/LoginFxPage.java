package ui.fx;

import dao.AppUserDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import model.AppUser;

import java.util.function.Consumer;

public class LoginFxPage extends BorderPane {

    private final AppUserDAO userDAO = new AppUserDAO();
    private final Consumer<AppUser> loginHandler;

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label messageLabel = new Label();

    public LoginFxPage(Consumer<AppUser> loginHandler) {
        this.loginHandler = loginHandler;
        getStyleClass().add("login-root");
        setCenter(createLoginCard());
    }

    private VBox createLoginCard() {
        Label title = new Label("SAFAD");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Sales & Warehouse Management System");
        subtitle.getStyleClass().add("login-subtitle");

        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setOnAction(e -> login());

        messageLabel.getStyleClass().add("login-error");
        messageLabel.setMinHeight(18);

        Button loginButton = FxTheme.primaryButton("Login");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> login());

        VBox card = new VBox(10, title, subtitle, usernameField, passwordField, messageLabel, loginButton);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        BorderPane.setAlignment(card, Pos.CENTER);
        BorderPane.setMargin(card, new Insets(30));

        return card;
    }

    private void login() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Enter username and password.");
            return;
        }

        AppUser user = userDAO.getActiveUserByUsername(username);
        if (user == null || !PasswordUtil.passwordMatches(password, user.getPasswordSalt(), user.getPasswordHash())) {
            showError("Invalid username or password.");
            passwordField.clear();
            return;
        }

        messageLabel.setText("");
        loginHandler.accept(user);
    }

    private void showError(String message) {
        messageLabel.setText(message);
    }
}
