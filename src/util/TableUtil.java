package util;

import ui.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TableUtil {

    private static final Map<JTable, Map<Object, TableColumn>> hiddenColumns = new HashMap<>();

    public static DefaultTableModel createNonEditableTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void setupTable(JTable table) {
        UIStyle.styleTable(table);
        table.setAutoCreateRowSorter(true);
    }

    public static void clearTable(DefaultTableModel model) {
        model.setRowCount(0);
    }

    public static int getSelectedRowId(JTable table, int idColumnIndex) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return -1;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Object value = table.getModel().getValueAt(modelRow, idColumnIndex);

        if (value instanceof Integer) return (Integer) value;
        return Integer.parseInt(value.toString());
    }

    public static JButton createColumnVisibilityButton(JTable table, String buttonText) {
        JButton button = new JButton(buttonText);
        UIStyle.styleSecondaryButton(button);
        button.addActionListener(e -> showColumnVisibilityMenu(table, button));
        return button;
    }

    public static void showColumnVisibilityMenu(JTable table, JComponent invoker) {
        JPopupMenu menu = new JPopupMenu();

        Map<Object, TableColumn> knownColumns = getKnownColumns(table);

        for (Object identifier : knownColumns.keySet()) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                    identifier.toString(),
                    isColumnVisible(table, identifier)
            );

            item.addActionListener(e -> setColumnVisible(table, identifier, item.isSelected()));
            menu.add(item);
        }

        menu.show(invoker, 0, invoker.getHeight());
    }

    public static void setColumnVisible(JTable table, Object identifier, boolean visible) {
        hiddenColumns.putIfAbsent(table, new LinkedHashMap<>());

        if (visible) {
            TableColumn hidden = hiddenColumns.get(table).remove(identifier);
            if (hidden != null) {
                table.getColumnModel().addColumn(hidden);
            }
            return;
        }

        TableColumn visibleColumn = getVisibleColumn(table, identifier);
        if (visibleColumn != null) {
            hiddenColumns.get(table).put(identifier, visibleColumn);
            table.getColumnModel().removeColumn(visibleColumn);
        }
    }

    private static Map<Object, TableColumn> getKnownColumns(JTable table) {
        Map<Object, TableColumn> result = new LinkedHashMap<>();

        TableColumnModel model = table.getColumnModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = model.getColumn(i);
            result.put(column.getIdentifier(), column);
        }

        if (hiddenColumns.containsKey(table)) {
            result.putAll(hiddenColumns.get(table));
        }

        return result;
    }

    private static boolean isColumnVisible(JTable table, Object identifier) {
        return getVisibleColumn(table, identifier) != null;
    }

    private static TableColumn getVisibleColumn(JTable table, Object identifier) {
        TableColumnModel model = table.getColumnModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = model.getColumn(i);
            if (identifier.equals(column.getIdentifier())) {
                return column;
            }
        }

        return null;
    }
}
