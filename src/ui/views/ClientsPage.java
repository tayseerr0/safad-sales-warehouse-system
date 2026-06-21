package ui.views;

import dao.ClientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Client;
import ui.TableUtil;
import ui.Theme;

import java.time.LocalDate;

public class ClientsPage extends VBox {

    private final ClientDAO clientDAO = new ClientDAO();
    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    private final TextField idField = Theme.textField("ID");
    private final TextField nameField = Theme.textField("Client name");
    private final TextField phoneField = Theme.textField("Phone");
    private final TextField emailField = Theme.textField("Email");
    private final DatePicker registrationDatePicker = new DatePicker(LocalDate.now());
    private final TextField cityField = Theme.textField("City");
    private final TextField addressField = Theme.textField("Address");
    private final ComboBox<String> typeComboBox = new ComboBox<>();
    private final TextField searchField = Theme.textField("Search clients");
    private final ComboBox<String> searchColumnBox = TableUtil.searchColumnBox();
    private final TableView<Client> table = new TableView<>();
    private Button clientActionButton;
    private Button deleteButton;

    public ClientsPage() {
        Theme.styleComboBox(typeComboBox);
        typeComboBox.getStyleClass().add("compact-selector");
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadClients();
    }

    private BorderPane createContent() {
        idField.setEditable(false);
        typeComboBox.setItems(FXCollections.observableArrayList("Individual", "Company", "Reseller"));
        typeComboBox.getSelectionModel().selectFirst();

        configureTable();

        return Theme.ledgerWorkspace(
                Theme.ledgerSurface("Client Ledger", createToolbar(), table),
                Theme.ledgerInspector("Client Inspector", createForm())
        );
    }

    private HBox createToolbar() {
        Button refreshButton = Theme.refreshButton();
        refreshButton.setOnAction(e -> {
            searchField.clear();
            loadClients();
        });

        HBox toolbar = Theme.ledgerCommandBar(searchColumnBox, searchField, refreshButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return toolbar;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        addRow(form, 0, "ID", idField);
        addRow(form, 1, "Name", nameField);
        addRow(form, 2, "Phone", phoneField);
        addRow(form, 3, "Email", emailField);
        addRow(form, 4, "Registration", registrationDatePicker);
        addRow(form, 5, "City", cityField);
        addRow(form, 6, "Address", addressField);
        addRow(form, 7, "Type", typeComboBox);

        clientActionButton = Theme.primaryButton("Add");
        deleteButton = Theme.dangerButton("Delete");
        Button clearButton = Theme.secondaryButton("Clear");

        clientActionButton.setOnAction(e -> saveClient());
        deleteButton.setOnAction(e -> deleteClient());
        clearButton.setOnAction(e -> clearForm());
        Theme.setVisible(deleteButton, false);

        form.add(Theme.compactActionRow(clientActionButton, deleteButton, clearButton), 0, 8, 2, 1);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new javafx.scene.control.Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTable() {
        table.getColumns().add(TableUtil.column("ID", Client::getClientId, 70));
        table.getColumns().add(TableUtil.column("Name", Client::getClientName, 160));
        table.getColumns().add(TableUtil.column("Phone", Client::getPhone, 120));
        table.getColumns().add(TableUtil.column("Email", Client::getEmail, 190));
        table.getColumns().add(TableUtil.column("Registration", Client::getRegistrationDate, 120));
        table.getColumns().add(TableUtil.column("City", Client::getCity, 110));
        table.getColumns().add(TableUtil.column("Address", Client::getAddress, 180));
        table.getColumns().add(TableUtil.column("Type", Client::getClientType, 100));
        TableUtil.installSearch(table, clients, searchField, searchColumnBox);
        Theme.styleTable(table);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, client) -> {
            if (client != null) {
                fillForm(client);
            }
        });
    }

    private void loadClients() {
        clients.setAll(clientDAO.getAllClients());
    }

    private void addClient() {
        Client client = readForm(false);
        if (client != null && clientDAO.addClient(client)) {
            Theme.showInfo("Client added successfully.");
            clearForm();
            loadClients();
        } else {
            Theme.showError("Failed to add client.");
        }
    }

    private void saveClient() {
        if (idField.getText().isBlank()) {
            addClient();
        } else {
            updateClient();
        }
    }

    private void updateClient() {
        Client client = readForm(true);
        if (client != null && clientDAO.updateClient(client)) {
            Theme.showInfo("Client updated successfully.");
            clearForm();
            loadClients();
        } else {
            Theme.showError("Failed to update client.");
        }
    }

    private void deleteClient() {
        if (idField.getText().isBlank()) {
            Theme.showError("Select a client first.");
            return;
        }

        if (Theme.confirm("Delete selected client?") && clientDAO.deleteClient(Integer.parseInt(idField.getText()))) {
            Theme.showInfo("Client deleted successfully.");
            clearForm();
            loadClients();
        }
    }

    private Client readForm(boolean includeId) {
        if (nameField.getText().trim().isEmpty()) {
            Theme.showError("Client name is required.");
            return null;
        }

        Client client = new Client(
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                registrationDatePicker.getValue(),
                cityField.getText().trim(),
                addressField.getText().trim(),
                typeComboBox.getValue()
        );

        if (includeId) {
            if (idField.getText().isBlank()) {
                Theme.showError("Select a client first.");
                return null;
            }
            client.setClientId(Integer.parseInt(idField.getText()));
        }

        return client;
    }

    private void fillForm(Client client) {
        idField.setText(String.valueOf(client.getClientId()));
        nameField.setText(client.getClientName());
        phoneField.setText(client.getPhone());
        emailField.setText(client.getEmail());
        registrationDatePicker.setValue(client.getRegistrationDate());
        cityField.setText(client.getCity());
        addressField.setText(client.getAddress());
        typeComboBox.setValue(client.getClientType());
        updateFormMode();
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        registrationDatePicker.setValue(LocalDate.now());
        cityField.clear();
        addressField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        table.getSelectionModel().clearSelection();
        updateFormMode();
    }

    private void updateFormMode() {
        boolean selected = !idField.getText().isBlank();
        if (clientActionButton != null) {
            clientActionButton.setText(selected ? "Update" : "Add");
        }
        if (deleteButton != null) {
            Theme.setVisible(deleteButton, selected);
        }
    }
}
