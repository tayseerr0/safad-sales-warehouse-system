package util;

import ui.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/*
Usage Example:

String[] columns = {"ID", "Name", "Phone", "City"};
DefaultTableModel model = TableUtil.createNonEditableTableModel(columns);

JTable table = new JTable(model);
TableUtil.setupTable(table);
 */

public class TableUtil {

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

        if (selectedRow == -1) {
            return -1;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Object value = table.getModel().getValueAt(modelRow, idColumnIndex);

        if (value instanceof Integer) {
            return (Integer) value;
        }

        return Integer.parseInt(value.toString());
    }
}