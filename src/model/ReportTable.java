package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReportTable {
    private final List<String> columns = new ArrayList<>();
    private final List<List<Object>> rows = new ArrayList<>();

    public ReportTable() {
    }

    public ReportTable(String[] columns, int ignoredRowCount) {
        this.columns.addAll(Arrays.asList(columns));
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public void addRow(Object[] values) {
        rows.add(new ArrayList<>(Arrays.asList(values)));
    }

    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        return rows.size();
    }

    public String getColumnName(int column) {
        return columns.get(column);
    }

    public Object getValueAt(int row, int column) {
        return rows.get(row).get(column);
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<List<Object>> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
