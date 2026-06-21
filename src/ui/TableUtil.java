package ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import model.ReportTable;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.function.Function;
import java.util.prefs.Preferences;

public class TableUtil {

    public static final String ALL_SEARCH_COLUMNS = "All columns";
    private static final String MODE_BOUND_KEY = "safadColumnModeBound";
    private static final String COLUMN_MODE_PREF = "safadColumnMode";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(TableUtil.class);
    private static final ObjectProperty<ColumnMode> GLOBAL_COLUMN_MODE = new SimpleObjectProperty<>(loadSavedColumnMode());

    private TableUtil() {
    }

    static {
        GLOBAL_COLUMN_MODE.addListener((obs, oldMode, newMode) -> {
            if (newMode != null) {
                PREFERENCES.put(COLUMN_MODE_PREF, newMode.name());
            }
        });
    }

    public static <T> TableColumn<T, Object> column(String title, Function<T, Object> valueFactory, double width) {
        TableColumn<T, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(valueFactory.apply(data.getValue())));
        column.setPrefWidth(width);
        column.setMinWidth(Math.min(90, width));
        configureSortableColumn(column);
        return column;
    }

    public static TableView<Map<String, Object>> tableFromModel(ReportTable model) {
        TableView<Map<String, Object>> table = new TableView<>();
        Theme.styleTable(table);
        fillFromModel(table, model);
        return table;
    }

    public static ObservableList<Map<String, Object>> fillFromModel(TableView<Map<String, Object>> table, ReportTable model) {
        table.getColumns().clear();

        for (int col = 0; col < model.getColumnCount(); col++) {
            String columnName = model.getColumnName(col);
            TableColumn<Map<String, Object>, Object> column = new TableColumn<>(columnName);
            column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().get(columnName)));
            column.setPrefWidth(Math.max(120, columnName.length() * 12));
            column.setMinWidth(90);
            configureSortableColumn(column);
            table.getColumns().add(column);
        }

        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();

        for (int row = 0; row < model.getRowCount(); row++) {
            Map<String, Object> rowMap = new LinkedHashMap<>();

            for (int col = 0; col < model.getColumnCount(); col++) {
                rowMap.put(model.getColumnName(col), model.getValueAt(row, col));
            }

            rows.add(rowMap);
        }

        SortedList<Map<String, Object>> sorted = new SortedList<>(rows);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        bindColumnMode(table);
        applyColumnMode(table, getColumnMode());
        return rows;
    }

    public static <T> FilteredList<T> installSearch(TableView<T> table,
                                                    ObservableList<T> source,
                                                    TextField searchField) {
        return installSearch(table, source, searchField, null);
    }

    public static <T> FilteredList<T> installSearch(TableView<T> table,
                                                    ObservableList<T> source,
                                                    TextField searchField,
                                                    ComboBox<String> searchColumnBox) {
        FilteredList<T> filtered = new FilteredList<>(source, row -> true);
        SortedList<T> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        if (searchColumnBox != null) {
            populateSearchColumns(searchColumnBox, table);
        }

        Runnable applyFilter = () -> filtered.setPredicate(row ->
                rowMatches(table, row, searchField.getText(), selectedSearchColumn(searchColumnBox))
        );

        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilter.run());
        if (searchColumnBox != null) {
            searchColumnBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter.run());
        }

        return filtered;
    }

    public static ComboBox<String> searchColumnBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        Theme.styleComboBox(comboBox);
        comboBox.getStyleClass().add("compact-selector");
        comboBox.setMinWidth(130);
        comboBox.setPrefWidth(150);
        comboBox.setMaxWidth(190);
        comboBox.setPromptText("Search in");
        comboBox.getItems().setAll(ALL_SEARCH_COLUMNS);
        comboBox.getSelectionModel().select(ALL_SEARCH_COLUMNS);
        return comboBox;
    }

    public static <T> void populateSearchColumns(ComboBox<String> comboBox, TableView<T> table) {
        String previous = comboBox.getValue();
        comboBox.getItems().setAll(ALL_SEARCH_COLUMNS);

        for (TableColumn<T, ?> column : table.getColumns()) {
            String title = column.getText();
            if (title != null && !title.isBlank()) {
                comboBox.getItems().add(title);
            }
        }

        if (previous != null && comboBox.getItems().contains(previous)) {
            comboBox.getSelectionModel().select(previous);
        } else {
            comboBox.getSelectionModel().select(ALL_SEARCH_COLUMNS);
        }
    }

    public static void populateSearchColumns(ComboBox<String> comboBox, ReportTable model) {
        String previous = comboBox.getValue();
        comboBox.getItems().setAll(ALL_SEARCH_COLUMNS);

        if (model != null) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                comboBox.getItems().add(model.getColumnName(col));
            }
        }

        if (previous != null && comboBox.getItems().contains(previous)) {
            comboBox.getSelectionModel().select(previous);
        } else {
            comboBox.getSelectionModel().select(ALL_SEARCH_COLUMNS);
        }
    }

    private static <T> void configureSortableColumn(TableColumn<T, Object> column) {
        column.setSortable(true);
        column.setComparator(TableUtil::compareValues);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compareValues(Object left, Object right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        if (left instanceof String || right instanceof String) {
            return String.CASE_INSENSITIVE_ORDER.compare(left.toString(), right.toString());
        }
        if (left instanceof Comparable comparable && left.getClass().isInstance(right)) {
            return comparable.compareTo(right);
        }
        return Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER).compare(left, right);
    }

    public static HBox globalColumnModeControls() {
        ToggleGroup group = new ToggleGroup();
        RadioButton simple = columnModeRadio("Simple", ColumnMode.SIMPLE, group);
        RadioButton detailed = columnModeRadio("Detailed", ColumnMode.DETAILED, group);

        if (getColumnMode() == ColumnMode.SIMPLE) {
            simple.setSelected(true);
        } else {
            detailed.setSelected(true);
        }

        GLOBAL_COLUMN_MODE.addListener((obs, oldMode, newMode) -> {
            if (newMode == ColumnMode.SIMPLE) {
                simple.setSelected(true);
            } else {
                detailed.setSelected(true);
            }
        });

        HBox controls = new HBox(8, simple, detailed);
        controls.getStyleClass().add("column-mode-controls");
        return controls;
    }

    public static <T> HBox columnModeBox(TableView<T> table) {
        bindColumnMode(table);
        return globalColumnModeControls();
    }

    private static RadioButton columnModeRadio(String text, ColumnMode mode, ToggleGroup group) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.setToggleGroup(group);
        radioButton.getStyleClass().add("column-mode-radio");
        radioButton.setOnAction(e -> setColumnMode(mode));
        return radioButton;
    }

    public static ObjectProperty<ColumnMode> columnModeProperty() {
        return GLOBAL_COLUMN_MODE;
    }

    public static ColumnMode getColumnMode() {
        return GLOBAL_COLUMN_MODE.get();
    }

    public static void setColumnMode(ColumnMode mode) {
        GLOBAL_COLUMN_MODE.set(mode == null ? ColumnMode.DETAILED : mode);
    }

    public static <T> void bindColumnMode(TableView<T> table) {
        if (Boolean.TRUE.equals(table.getProperties().get(MODE_BOUND_KEY))) {
            return;
        }

        table.getProperties().put(MODE_BOUND_KEY, true);
        GLOBAL_COLUMN_MODE.addListener((obs, oldMode, newMode) -> applyColumnMode(table, newMode));
        applyColumnMode(table, getColumnMode());
    }

    public static <T> void applyColumnMode(TableView<T> table, ColumnMode mode) {
        if (mode == null) {
            mode = ColumnMode.DETAILED;
        }

        List<TableColumn<T, ?>> columns = new ArrayList<>(table.getColumns());

        for (TableColumn<T, ?> column : columns) {
            column.setVisible(shouldShow(column.getText(), mode, columns));
        }

        boolean anyVisible = columns.stream().anyMatch(TableColumn::isVisible);
        if (!anyVisible && !columns.isEmpty()) {
            columns.get(0).setVisible(true);
        }
    }

    private static <T> boolean rowMatches(TableView<T> table, T row, String keyword, String selectedColumn) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase();

        if (text.isEmpty()) {
            return true;
        }

        for (TableColumn<T, ?> column : table.getColumns()) {
            if (!matchesSelectedColumn(column.getText(), selectedColumn)) {
                continue;
            }

            Object value = column.getCellObservableValue(row) == null
                    ? null
                    : column.getCellObservableValue(row).getValue();

            if (value != null && value.toString().toLowerCase().contains(text)) {
                return true;
            }
        }

        return false;
    }

    private static String selectedSearchColumn(ComboBox<String> searchColumnBox) {
        return searchColumnBox == null ? ALL_SEARCH_COLUMNS : searchColumnBox.getValue();
    }

    private static boolean matchesSelectedColumn(String column, String selectedColumn) {
        return selectedColumn == null
                || ALL_SEARCH_COLUMNS.equals(selectedColumn)
                || selectedColumn.equals(column);
    }

    private static <T> boolean shouldShow(String title, ColumnMode mode, List<TableColumn<T, ?>> columns) {
        if (mode == ColumnMode.DETAILED) {
            return true;
        }

        boolean detailed = isDetailedColumn(title);
        return !detailed || columns.size() <= 3;
    }

    private static boolean isDetailedColumn(String title) {
        String name = title == null ? "" : title.toLowerCase();
        return name.equals("id")
                || name.endsWith(" id")
                || name.contains("item id")
                || name.contains("invoice id")
                || name.contains("transfer id")
                || name.contains("phone")
                || name.contains("email")
                || name.contains("address")
                || name.contains("description")
                || name.contains("threshold")
                || name.contains("warranty")
                || name.contains("payment")
                || name.contains("arrival")
                || name.contains("registration");
    }

    private static ColumnMode loadSavedColumnMode() {
        String saved = PREFERENCES.get(COLUMN_MODE_PREF, ColumnMode.DETAILED.name());

        if ("ALL".equals(saved)) {
            return ColumnMode.DETAILED;
        }

        try {
            return ColumnMode.valueOf(saved);
        } catch (IllegalArgumentException e) {
            return ColumnMode.DETAILED;
        }
    }

    public enum ColumnMode {
        SIMPLE("Simple"),
        DETAILED("Detailed");

        private final String label;

        ColumnMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
