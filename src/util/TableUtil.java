package util;

import ui.UIStyle;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TableUtil {

    private static final Map<JTable, Map<Object, TableColumn>> hiddenColumns = new HashMap<>();
    private static final String HEADER_POPUP_PROPERTY = "safadHeaderPopupInstalled";

    public static DefaultTableModel createNonEditableTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void setupTable(JTable table) {
        hiddenColumns.remove(table);
        UIStyle.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setComponentPopupMenu(createTablePopupMenu(table));
        installHeaderPopup(table);
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

        JMenuItem simpleMode = new JMenuItem("Simple columns");
        JMenuItem detailedMode = new JMenuItem("Detailed columns");
        JMenuItem allMode = new JMenuItem("Simple + Detailed");

        simpleMode.addActionListener(e -> applyColumnDisplayMode(table, DisplayMode.SIMPLE));
        detailedMode.addActionListener(e -> applyColumnDisplayMode(table, DisplayMode.DETAILED));
        allMode.addActionListener(e -> applyColumnDisplayMode(table, DisplayMode.ALL));

        menu.add(simpleMode);
        menu.add(detailedMode);
        menu.add(allMode);
        menu.addSeparator();

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

    public static void installLiveSearch(JTextField searchField, JTable table) {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter(table, searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter(table, searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter(table, searchField.getText());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void applySearchFilter(JTable table, String keyword) {
        TableRowSorter<TableModel> sorter;

        if (table.getRowSorter() instanceof TableRowSorter<?>) {
            sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        } else {
            sorter = new TableRowSorter<>(table.getModel());
            table.setRowSorter(sorter);
        }

        String text = keyword == null ? "" : keyword.trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }

    public static void applyColumnDisplayMode(JTable table, DisplayMode displayMode) {
        Map<Object, TableColumn> knownColumns = new LinkedHashMap<>(getKnownColumns(table));

        for (Object identifier : knownColumns.keySet()) {
            setColumnVisible(table, identifier, shouldShowColumn(identifier, displayMode, knownColumns));
        }

        if (table.getColumnModel().getColumnCount() == 0) {
            for (Object identifier : knownColumns.keySet()) {
                setColumnVisible(table, identifier, true);
                break;
            }
        }
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

    private static JPopupMenu createTablePopupMenu(JTable table) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem simpleMode = new JMenuItem("Simple columns");
        JMenuItem detailedMode = new JMenuItem("Detailed columns");
        JMenuItem allMode = new JMenuItem("Simple + Detailed");
        JMenuItem columns = new JMenuItem("Choose columns...");

        simpleMode.addActionListener(e -> applyColumnDisplayMode(table, DisplayMode.SIMPLE));
        detailedMode.addActionListener(e -> applyColumnDisplayMode(table, DisplayMode.DETAILED));
        allMode.addActionListener(e -> applyColumnDisplayMode(table, DisplayMode.ALL));
        columns.addActionListener(e -> showColumnVisibilityMenu(table, table));

        menu.add(simpleMode);
        menu.add(detailedMode);
        menu.add(allMode);
        menu.addSeparator();
        menu.add(columns);

        return menu;
    }

    private static void installHeaderPopup(JTable table) {
        if (Boolean.TRUE.equals(table.getClientProperty(HEADER_POPUP_PROPERTY))) {
            return;
        }

        table.putClientProperty(HEADER_POPUP_PROPERTY, true);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            private void showPopupIfNeeded(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    createTablePopupMenu(table).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private static boolean shouldShowColumn(Object identifier,
                                            DisplayMode displayMode,
                                            Map<Object, TableColumn> knownColumns) {
        if (displayMode == DisplayMode.ALL) {
            return true;
        }

        boolean advanced = isAdvancedColumn(identifier);

        if (displayMode == DisplayMode.SIMPLE) {
            return !advanced || knownColumns.size() <= 3;
        }

        return advanced || !hasUsefulAdvancedColumns(knownColumns);
    }

    private static boolean hasUsefulAdvancedColumns(Map<Object, TableColumn> knownColumns) {
        int advancedCount = 0;

        for (Object identifier : knownColumns.keySet()) {
            if (isAdvancedColumn(identifier)) {
                advancedCount++;
            }
        }

        return advancedCount > 0 && advancedCount < knownColumns.size();
    }

    private static boolean isAdvancedColumn(Object identifier) {
        String name = identifier == null ? "" : identifier.toString().toLowerCase();

        return name.equals("id")
                || name.endsWith(" id")
                || name.contains(" item id")
                || name.contains(" invoice id")
                || name.contains(" transfer id")
                || name.contains("phone")
                || name.contains("email")
                || name.contains("address")
                || name.contains("description")
                || name.contains("threshold")
                || name.contains("estimated arrival")
                || name.contains("warranty")
                || name.equals("payment")
                || name.contains("payment type")
                || name.contains("starting date");
    }

    public enum DisplayMode {
        SIMPLE,
        DETAILED,
        ALL
    }
}
