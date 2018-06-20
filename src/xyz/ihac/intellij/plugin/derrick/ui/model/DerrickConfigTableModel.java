package xyz.ihac.intellij.plugin.derrick.ui.model;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DerrickConfigTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "key",
            "value"
    };
    private final Class[] columnClasses = {
            String.class,
            String.class
    };

    private final List<Map<String, String>> params = new LinkedList<>();
    private int imageNameRow;

    public DerrickConfigTableModel() {}

    public DerrickConfigTableModel(List<Map<String, String>> params) {
        setParams(params);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // allow user to modify value.
        return columnIndex != 0;
    }

    @Override
    public int getRowCount() {
        return params.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Map<String, String> config = params.get(rowIndex);
        switch (columnIndex) {
            case 0: return config.get(columnNames[0]);
            case 1: return config.get(columnNames[1]);
            default:
                throw new IllegalArgumentException(String.format("Unable to get value at (%d, %d) in DerrickConfig Table", rowIndex, columnIndex));
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Map<String, String> config = params.get(rowIndex);
        switch (columnIndex) {
            case 1:
                config.put(columnNames[1], (String) aValue);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unable to set value at (%d, %d) in DerrickConfig Table", rowIndex, columnIndex));
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void setParams(List<Map<String, String>> params) {
        this.params.clear();
        int cnt = 0, index = -1;
        for (Map<String, String> param : params) {
            this.params.add(new HashMap<>(param));
            if (param.get(columnNames[0]).equals("image name")) {
                index = cnt;
            }
            cnt++;
        }
        imageNameRow = index;
    }

    public List<Map<String, String>> getRawParams() {
        return params;
    }

    public Map<String, String> getParams() {
        HashMap<String, String> res = new HashMap<>();
        for (Map<String, String> param : params) {
            res.put(param.get(columnNames[0]), param.get(columnNames[1]));
        }
        return res;
    }

    public int getImageNameRow() {
        return imageNameRow;
    }
}
