package ui.fx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.function.Function;
import java.util.prefs.Preferences;

public class FxTableUtil {

    private static final String MODE_BOUND_KEY = "safadColumnModeBound";
    private static final String COLUMN_MODE_PREF = "safadColumnMode";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(FxTableUtil.class);
    private static final ObjectProperty<ColumnMode> GLOBAL_COLUMN_MODE = new SimpleObjectProperty<>(loadSavedColumnMode());

    private FxTableUtil() {
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

    public static TableView<Map<String, Object>> tableFromModel(DefaultTableModel model) {
        TableView<Map<String, Object>> table = new TableView<>();
        FxTheme.styleTable(table);
        fillFromModel(table, model);
        return table;
    }

    public static ObservableList<Map<String, Object>> fillFromModel(TableView<Map<String, Object>> table, DefaultTableModel model) {
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
        FilteredList<T> filtered = new FilteredList<>(source, row -> true);
        SortedList<T> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        searchField.textProperty().addListener((obs, oldText, newText) ->
                filtered.setPredicate(row -> rowMatches(table, row, newText))
        );

        return filtered;
    }

    private static <T> void configureSortableColumn(TableColumn<T, Object> column) {
        column.setSortable(true);
        column.setComparator(FxTableUtil::compareValues);
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

    public static ComboBox<ColumnMode> globalColumnModeBox() {
        ComboBox<ColumnMode> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(ColumnMode.SIMPLE, ColumnMode.DETAILED, ColumnMode.ALL);
        comboBox.valueProperty().bindBidirectional(GLOBAL_COLUMN_MODE);
        comboBox.getStyleClass().add("clean-selector");
        comboBox.getStyleClass().add("column-mode-box");
        return comboBox;
    }

    public static <T> ComboBox<ColumnMode> columnModeBox(TableView<T> table) {
        bindColumnMode(table);
        return globalColumnModeBox();
    }

    public static ObjectProperty<ColumnMode> columnModeProperty() {
        return GLOBAL_COLUMN_MODE;
    }

    public static ColumnMode getColumnMode() {
        return GLOBAL_COLUMN_MODE.get();
    }

    public static void setColumnMode(ColumnMode mode) {
        GLOBAL_COLUMN_MODE.set(mode == null ? ColumnMode.ALL : mode);
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
            mode = ColumnMode.ALL;
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

    private static <T> boolean rowMatches(TableView<T> table, T row, String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase();

        if (text.isEmpty()) {
            return true;
        }

        for (TableColumn<T, ?> column : table.getColumns()) {
            Object value = column.getCellObservableValue(row) == null
                    ? null
                    : column.getCellObservableValue(row).getValue();

            if (value != null && value.toString().toLowerCase().contains(text)) {
                return true;
            }
        }

        return false;
    }

    private static <T> boolean shouldShow(String title, ColumnMode mode, List<TableColumn<T, ?>> columns) {
        if (mode == ColumnMode.ALL) {
            return true;
        }

        boolean detailed = isDetailedColumn(title);

        if (mode == ColumnMode.SIMPLE) {
            return !detailed || columns.size() <= 3;
        }

        return detailed || columns.stream().noneMatch(column -> isDetailedColumn(column.getText()));
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
        String saved = PREFERENCES.get(COLUMN_MODE_PREF, ColumnMode.ALL.name());

        try {
            return ColumnMode.valueOf(saved);
        } catch (IllegalArgumentException e) {
            return ColumnMode.ALL;
        }
    }

    public enum ColumnMode {
        SIMPLE("Simple"),
        DETAILED("Detailed"),
        ALL("Simple + Detailed");

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
