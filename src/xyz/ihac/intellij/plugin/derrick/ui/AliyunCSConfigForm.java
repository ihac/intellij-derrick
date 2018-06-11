package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.StripeTable;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.addon.AliyunCS;
import xyz.ihac.intellij.plugin.derrick.util.NonEmpty;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class AliyunCSConfigForm extends DialogWrapper {
    private JPanel rootPanel;
    private JPanel setAccessKeyPanel;
    private JPanel selectClusterPanel;

    private JTextField accessKeyIDTextField;
    private JPasswordField accessKeySecretPasswdField;
    private JTextField regionTextField;
    private JLabel accessKeyIDLabel;
    private JLabel accessKeySecretLabel;
    private JLabel regionLabel;

    private JBTable clusterTable;

    private Boolean selectClusterMode;

    public AliyunCSConfigForm(@Nullable Project project, Boolean selectCluster) {
        super(project);
        super.init();
        super.setTitle("Aliyun Container Service Settings");

        selectClusterMode = selectCluster;
        initComponent();
    }

    public AliyunCSConfigForm(@Nullable Project project, List<AliyunCSCluster> clusters) {
        this(project, true);

        initDefaultValue(clusters);
    }

    private void initDefaultValue(List<AliyunCSCluster> clusters) {
        AliyunCSClusterTableModel model = new AliyunCSClusterTableModel(clusters);
        clusterTable.setModel(model);
        clusterTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        clusterTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        clusterTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        clusterTable.getColumnModel().getColumn(3).setPreferredWidth(200);
    }

    private void initComponent() {
        rootPanel.setMinimumSize(new Dimension(400, 100));
        if (selectClusterMode) {
            setAccessKeyPanel.setVisible(false);
            selectClusterPanel.setVisible(true);

            clusterTable = new JBTable();
            clusterTable.getEmptyText().setText("No kubernetes cluster found");
            StripeTable.apply(clusterTable);

            ToolbarDecorator decorator = ToolbarDecorator.createDecorator(clusterTable);
            decorator.setPreferredSize(new Dimension(-1, 100));
            clusterTable.setVisible(true);
            selectClusterPanel.setLayout(new BoxLayout(selectClusterPanel, BoxLayout.Y_AXIS));
            selectClusterPanel.add(decorator.createPanel());
        }
        else {
            setAccessKeyPanel.setVisible(true);
            selectClusterPanel.setVisible(false);
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (!selectClusterMode) {
            if (!NonEmpty.verify(getRegion()))
                return new ValidationInfo("Please enter valid region", regionTextField);
            if (!NonEmpty.verify(getAccessKeyID()))
                return new ValidationInfo("Please enter valid access key ID", accessKeyIDTextField);
            if (!NonEmpty.verify(getAccessKeySecret()))
                return new ValidationInfo("Please enter valid access key secret", accessKeySecretPasswdField);
        }
        return super.doValidate();
    }

    public String getRegion() {
        return regionTextField.getText().trim();
    }

    public String getAccessKeyID() {
        return accessKeyIDTextField.getText().trim();
    }

    public String getAccessKeySecret() {
        return String.valueOf(accessKeySecretPasswdField.getPassword()).trim();
    }

    public List<AliyunCSCluster> getSelectedClusters() {
        AliyunCSClusterTableModel model = (AliyunCSClusterTableModel) clusterTable.getModel();
        return model.getSelectedCluster();
    }
}
