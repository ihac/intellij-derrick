package xyz.ihac.intellij.plugin.derrick.ui;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;

public class AliyunCSClusterTableModel extends AbstractTableModel {
    private final String[] columnNames = {
            "",
            "Name",
            "Cluster ID",
            "API Server"
    };
    private final Class[] columnClasses = {
            Boolean.class,
            String.class,
            String.class,
            String.class
    };
    private class AliyunCSClusterConfiguration {
        private Boolean isSelected;
        private AliyunCSCluster cluster;

        public AliyunCSClusterConfiguration(AliyunCSCluster cluster, Boolean isSelected) {
            this.isSelected = isSelected;
            this.cluster = cluster;
        }

        public Boolean isSelected() {
            return isSelected;
        }

        public AliyunCSCluster getCluster() {
            return cluster;
        }

        public void setSelected(Boolean aValue) {
            isSelected = aValue;
        }

        public void setCluster(AliyunCSCluster cluster) {
            this.cluster = cluster;
        }
    }
    private List<AliyunCSClusterConfiguration> clusterConfigurations = new LinkedList<AliyunCSClusterConfiguration>();

    public AliyunCSClusterTableModel(List<AliyunCSCluster> clusters) {
        for (AliyunCSCluster cluster : clusters) {
            // only support kubernetes for now.
            if (cluster.ctype().equals("Kubernetes"))
                clusterConfigurations.add(new AliyunCSClusterConfiguration(cluster, false));
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // allow user to select/deselect a cluster.
        return columnIndex <= 1;
    }

    @Override
    public int getRowCount() {
        return clusterConfigurations.size();
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
        AliyunCSClusterConfiguration config = clusterConfigurations.get(rowIndex);
        AliyunCSCluster cluster = config.getCluster();
        switch (columnIndex) {
            case 0: return config.isSelected();
            case 1: return cluster.name();
            case 2: return cluster.id();
            case 3: return cluster.masterUrl();
            default:
                throw new IllegalArgumentException(String.format("Unable to get value at (%d, %d) in AliyunCSCluster Table", rowIndex, columnIndex));
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        AliyunCSClusterConfiguration config = clusterConfigurations.get(rowIndex);
        AliyunCSCluster cluster = config.getCluster();
        switch (columnIndex) {
            case 0: config.setSelected((Boolean) aValue); break;
            case 1: config.setCluster(new AliyunCSCluster(
                    (String) aValue,
                    cluster.id(),
                    cluster.ctype(),
                    cluster.masterUrl()
            )); break;
            default:
                throw new IllegalArgumentException(String.format("Unable to set value at (%d, %d) in AliyunCSCluster Table", rowIndex, columnIndex));
        }
    }

    public List<AliyunCSCluster> getSelectedCluster() {
        LinkedList<AliyunCSCluster> result = new LinkedList<>();
        for (AliyunCSClusterConfiguration config: clusterConfigurations) {
            if (config.isSelected()) {
                result.add(config.getCluster());
            }
        }
        return result;
    }
}
