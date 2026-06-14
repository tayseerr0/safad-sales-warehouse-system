package ui;

import dao.ClientDAO;
import model.Client;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ClientPanel extends JPanel {

    private final ClientDAO clientDAO = new ClientDAO();

    private JTextField clientIdField;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField registrationDateField;
    private JTextField cityField;
    private JTextField addressField;
    private JComboBox<String> clientTypeComboBox;
    private JTextField searchField;
    private JComboBox<String> filterTypeComboBox;

    private JTable clientTable;
    private DefaultTableModel tableModel;

    public ClientPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadClients();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(UIStyle.createTitle("Client Management"));
        panel.add(UIStyle.createSubtitle("Add, update, delete, search, and filter SAFAD clients."));

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        formPanel.setMinimumSize(new Dimension(250, 0));
        tablePanel.setMinimumSize(new Dimension(500, 0));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                formPanel,
                tablePanel
        );

        splitPane.setResizeWeight(0.30);
        splitPane.setDividerLocation(330);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);
        wrapper.setMinimumSize(new Dimension(250, 0));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        clientIdField = new JTextField();
        clientIdField.setEditable(false);

        nameField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        registrationDateField = new JTextField(LocalDate.now().toString());
        cityField = new JTextField();
        addressField = new JTextField();

        clientTypeComboBox = new JComboBox<>(new String[]{"Individual", "Company", "Reseller"});

        addLabeledField(formPanel, gbc, "Client ID", clientIdField);
        addLabeledField(formPanel, gbc, "Client Name *", nameField);
        addLabeledField(formPanel, gbc, "Phone", phoneField);
        addLabeledField(formPanel, gbc, "Email", emailField);
        addLabeledField(formPanel, gbc, "Registration Date (YYYY-MM-DD) *", registrationDateField);
        addLabeledField(formPanel, gbc, "City", cityField);
        addLabeledField(formPanel, gbc, "Address", addressField);
        addLabeledField(formPanel, gbc, "Client Type *", clientTypeComboBox);

        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(createButtonPanel(), BorderLayout.SOUTH);

        return wrapper;
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.LABEL_FONT);
        label.setForeground(UIStyle.TEXT_DARK);

        UIStyle.styleTextFieldIfPossible(field);

        gbc.gridy++;
        panel.add(label, gbc);

        gbc.gridy++;
        panel.add(field, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(addButton);
        UIStyle.stylePrimaryButton(updateButton);
        UIStyle.stylePrimaryButton(deleteButton);
        UIStyle.stylePrimaryButton(clearButton);
        UIStyle.stylePrimaryButton(refreshButton);

        addButton.addActionListener(e -> addClient());
        updateButton.addActionListener(e -> updateClient());
        deleteButton.addActionListener(e -> deleteClient());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> loadClients());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createSearchPanel(), BorderLayout.NORTH);

        String[] columns = {
                "ID", "Name", "Phone", "Email", "Registration Date", "City", "Address", "Type"
        };

        tableModel = TableUtil.createNonEditableTableModel(columns);
        clientTable = new JTable(tableModel);
        TableUtil.setupTable(clientTable);

        clientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(clientTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(UIStyle.BACKGROUND);

        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        filterTypeComboBox = new JComboBox<>(new String[]{"All", "Individual", "Company", "Reseller"});
        JButton filterButton = new JButton("Filter");

        UIStyle.stylePrimaryButton(searchButton);
        UIStyle.stylePrimaryButton(filterButton);

        searchButton.addActionListener(e -> searchClients());
        filterButton.addActionListener(e -> filterClients());

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchButton);

        panel.add(new JLabel("Type:"));
        panel.add(filterTypeComboBox);
        panel.add(filterButton);

        return panel;
    }

    private void addClient() {
        try {
            Client client = readClientFromForm(false);

            boolean success = clientDAO.addClient(client);

            if (success) {
                MessageUtil.showSuccess("Client added successfully.");
                clearForm();
                loadClients();
            } else {
                MessageUtil.showError("Failed to add client.");
            }

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private void updateClient() {
        try {
            if (ValidationUtil.isEmpty(clientIdField.getText())) {
                MessageUtil.showWarning("Please select a client to update.");
                return;
            }

            Client client = readClientFromForm(true);

            boolean success = clientDAO.updateClient(client);

            if (success) {
                MessageUtil.showSuccess("Client updated successfully.");
                clearForm();
                loadClients();
            } else {
                MessageUtil.showError("Failed to update client.");
            }

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private void deleteClient() {
        if (ValidationUtil.isEmpty(clientIdField.getText())) {
            MessageUtil.showWarning("Please select a client to delete.");
            return;
        }

        boolean confirmed = MessageUtil.confirm("Are you sure you want to delete this client?");

        if (!confirmed) {
            return;
        }

        int clientId = Integer.parseInt(clientIdField.getText());

        boolean success = clientDAO.deleteClient(clientId);

        if (success) {
            MessageUtil.showSuccess("Client deleted successfully.");
            clearForm();
            loadClients();
        } else {
            MessageUtil.showError("Failed to delete client. This client may be linked to sales invoices.");
        }
    }

    private Client readClientFromForm(boolean includeId) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String registrationDateText = registrationDateField.getText().trim();
        String city = cityField.getText().trim();
        String address = addressField.getText().trim();
        String clientType = clientTypeComboBox.getSelectedItem().toString();

        if (ValidationUtil.isEmpty(name)) {
            throw new IllegalArgumentException("Client name is required.");
        }

        if (ValidationUtil.isEmpty(registrationDateText)) {
            throw new IllegalArgumentException("Registration date is required.");
        }

        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        LocalDate registrationDate;

        try {
            registrationDate = LocalDate.parse(registrationDateText);
        } catch (Exception e) {
            throw new IllegalArgumentException("Registration date must be in YYYY-MM-DD format.");
        }

        Client client = new Client(
                name,
                phone,
                email,
                registrationDate,
                city,
                address,
                clientType
        );

        if (includeId) {
            client.setClientId(Integer.parseInt(clientIdField.getText()));
        }

        return client;
    }

    private void loadClients() {
        List<Client> clients = clientDAO.getAllClients();
        fillTable(clients);
    }

    private void searchClients() {
        String keyword = searchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            loadClients();
            return;
        }

        List<Client> clients = clientDAO.searchClients(keyword);
        fillTable(clients);
    }

    private void filterClients() {
        String selectedType = filterTypeComboBox.getSelectedItem().toString();

        if (selectedType.equals("All")) {
            loadClients();
            return;
        }

        List<Client> clients = clientDAO.getClientsByType(selectedType);
        fillTable(clients);
    }

    private void fillTable(List<Client> clients) {
        TableUtil.clearTable(tableModel);

        for (Client client : clients) {
            tableModel.addRow(new Object[]{
                    client.getClientId(),
                    client.getClientName(),
                    client.getPhone(),
                    client.getEmail(),
                    client.getRegistrationDate(),
                    client.getCity(),
                    client.getAddress(),
                    client.getClientType()
            });
        }
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = clientTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = clientTable.convertRowIndexToModel(selectedRow);

        clientIdField.setText(tableModel.getValueAt(modelRow, 0).toString());
        nameField.setText(tableModel.getValueAt(modelRow, 1).toString());
        phoneField.setText(valueToString(tableModel.getValueAt(modelRow, 2)));
        emailField.setText(valueToString(tableModel.getValueAt(modelRow, 3)));
        registrationDateField.setText(valueToString(tableModel.getValueAt(modelRow, 4)));
        cityField.setText(valueToString(tableModel.getValueAt(modelRow, 5)));
        addressField.setText(valueToString(tableModel.getValueAt(modelRow, 6)));
        clientTypeComboBox.setSelectedItem(valueToString(tableModel.getValueAt(modelRow, 7)));
    }

    private String valueToString(Object value) {
        return value == null ? "" : value.toString();
    }

    private void clearForm() {
        clientIdField.setText("");
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        registrationDateField.setText(LocalDate.now().toString());
        cityField.setText("");
        addressField.setText("");
        clientTypeComboBox.setSelectedIndex(0);
        clientTable.clearSelection();
    }
}