package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.DerrickOptionProvider;
import xyz.ihac.intellij.plugin.derrick.DerrickProjectOptionProvider;
import xyz.ihac.intellij.plugin.derrick.DockerRegistryConfiguration;
import xyz.ihac.intellij.plugin.derrick.K8sClusterConfiguration;
import xyz.ihac.intellij.plugin.derrick.addon.Derrick;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Vector;

public class DerrickConfigForm extends DialogWrapper {
    private JPanel rootPanel;
    private JTable configTable;
    private JScrollPane scrollPanel;
    private JPanel basicConfigPanel;
    private JPanel checkBoxPanel;
    private JPanel pushConfigPanel;
    private JPanel deployConfigPanel;
    private JLabel imageNameLabel;

    private JCheckBox reBuildCheckBox;

    private JTextField imageNameTextField;
    private JPanel imageNamePanel;
    private JComboBox kubernetesClusterComboBox;
    private JLabel registryLabel;

    private JComboBox registryComboBox;
    private JLabel kubernetesClusterLabel;
    private JLabel deploymentYamlLabel;
    private JPanel deploymentYamlPathPanel;
    private JComboBox derrickRiggingComboBox;
    private JPanel derrickRiggingPanel;
    private JLabel derrickRiggingLabel;
    private TextFieldWithBrowseButton deploymentYamlTextField;

    private String action;

    public DerrickConfigForm(@Nullable Project project, String action) {
        super(project);
        super.init();
        super.setTitle("%s Config".format(action));

        this.action = action;

        scrollPanel.setBorder(BorderFactory.createEmptyBorder());
        scrollPanel.setVisible(false);
        pushConfigPanel.setVisible(false);
        deployConfigPanel.setVisible(false);
        imageNamePanel.setVisible(false);
        derrickRiggingPanel.setVisible(false);

        initComponents();
        initRiggings(project);
        initDefaultValue(project);
    }

    private void initRiggings(Project project) {
        DerrickOptionProvider option = ServiceManager.getService(DerrickOptionProvider.class);
        DerrickProjectOptionProvider projOption = ServiceManager.getService(project, DerrickProjectOptionProvider.class);
        java.util.Map<String, java.util.List<java.util.Map<String, String>>> riggingsAndParams =
                Derrick.get_riggings_and_params(option.getDerrickExecPath(), projOption.getWorkDir());
        derrickRiggingComboBox.addActionListener(e -> {
            String rigging = (String) derrickRiggingComboBox.getSelectedItem();
            initTable(riggingsAndParams.get(rigging));
        });
        for (String rigging: riggingsAndParams.keySet()) {
            derrickRiggingComboBox.addItem(rigging);
        }
    }

    public Boolean getIsRebuild() {
        return reBuildCheckBox.isSelected();
    }

    public String getRigging() {
        return (String) derrickRiggingComboBox.getSelectedItem();
    }

    public java.util.Map<String, String> getParams() {
        java.util.Map<String, String> res = new HashMap<String, String>();
        DefaultTableModel dtm = (DefaultTableModel) configTable.getModel();
        int nRow = dtm.getRowCount();
        for (int i = 0; i < nRow; i++) {
            String key = (String) dtm.getValueAt(i, 0);
            String value = (String) dtm.getValueAt(i, 1);
            res.put(key, value);
        }
        return res;
    }

    public String getDeploymentYaml() {
        return deploymentYamlTextField.getText();
    }

    public String getImageId() {
        return imageNameTextField.getText();
    }

    public DockerRegistryConfiguration getDockerRegistry() {
        return (DockerRegistryConfiguration) registryComboBox.getSelectedItem();
    }

    public K8sClusterConfiguration getK8sCluster() {
        return (K8sClusterConfiguration) kubernetesClusterComboBox.getSelectedItem();
    }

    private void initDefaultValue(Project project) {
        DerrickOptionProvider option = ServiceManager.getService(DerrickOptionProvider.class);
        for (DockerRegistryConfiguration registry: option.getDockerRegistries()) {
            registryComboBox.addItem(registry);
        }
        for (K8sClusterConfiguration cluster: option.getK8sClusters()) {
            kubernetesClusterComboBox.addItem(cluster);
        }

        deploymentYamlTextField.setText(project.getBasePath() + "/" + "kubernetes-deployment.yaml");
    }


    private void addCheckBoxListener() {
        reBuildCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                imageNameLabel.setEnabled(false);
                imageNameTextField.setEnabled(false);
            } else {
                imageNameLabel.setEnabled(true);
                imageNameTextField.setEnabled(true);
            }
        });
    }

    private TextFieldWithBrowseButton createTextFieldWithBrowseButton(String browserTitle, Boolean chooseDir) {
        LabeledComponent<TextFieldWithBrowseButton> labeledComponent = new LabeledComponent<>();
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(browserTitle, "", null,
                new FileChooserDescriptor(!chooseDir, chooseDir, false, false, false, false));
        return textField;
    }

    private void initComponents() {
        registryComboBox.addActionListener(e -> {
            String imageName = (String) configTable.getValueAt(0, 1);
            if (imageName.equals("")) return;

            String[] imageNameArr = imageName.split("/");
            DockerRegistryConfiguration registry = (DockerRegistryConfiguration) registryComboBox.getSelectedItem();
            String username = registry.getUsername();

            if (imageNameArr.length < 2)
                imageName = username + "/" + imageName;
            else {
                imageName = username + "/" + imageNameArr[1];
            }

            configTable.setValueAt(imageName, 0, 1);
        });

        deploymentYamlTextField = createTextFieldWithBrowseButton("Select deployment YAML", false);
        deploymentYamlPathPanel.setLayout(new BoxLayout(deploymentYamlPathPanel, BoxLayout.X_AXIS));
        deploymentYamlPathPanel.add(deploymentYamlTextField);

        basicConfigPanel.setLayout(new BoxLayout(basicConfigPanel, BoxLayout.Y_AXIS));
        if (action.equals("Init")) {
            checkBoxPanel.setVisible(false);
            pushConfigPanel.setVisible(true);
            derrickRiggingPanel.setVisible(true);
            scrollPanel.setVisible(true);
            Dimension preferredSize = rootPanel.getPreferredSize();
            this.setSize(preferredSize.width, preferredSize.height);
        }
        else {
            addCheckBoxListener();
            imageNamePanel.setVisible(true);
//            if (action.equals("Serve"))
//                serveConfigPanel.setVisible(true);
            if (action.equals("Push"))
                pushConfigPanel.setVisible(true);
            if (action.equals("Deploy")) {
                checkBoxPanel.setVisible(false);
                imageNamePanel.setVisible(false);
                deployConfigPanel.setVisible(true);
            }
            Dimension preferredSize = rootPanel.getPreferredSize();
            this.setSize(preferredSize.width, preferredSize.height);
        }
    }


    private void initTable(java.util.List<java.util.Map<String, String>> params) {
        Vector vData = new Vector();
        Vector vName = new Vector();
        vName.add("Key");
        vName.add("Value");
        for (java.util.Map<String, String> param: params) {
            String key = param.get("name");
            String description = param.get("description");
            String value = param.get("value");

            Vector vRow = new Vector();
            vRow.add(key);
            vRow.add(value);
            vData.add(vRow.clone());
        }

        DefaultTableModel model = new DefaultTableModel(vData, vName) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        model.addTableModelListener(e -> {
            if (e.getColumn() != 1 && e.getLastRow() != 0) return;
            String imageName = configTable.getValueAt(0, 1).toString();

            // validate the format of image name
            String[] imageNameArr = imageName.split("/");
            DockerRegistryConfiguration registry = (DockerRegistryConfiguration) registryComboBox.getSelectedItem();
            String username = registry.getUsername();

            if (imageNameArr.length < 2)
                imageName = username + "/" + imageName;
            else {
                imageName = username + "/" + imageNameArr[1];
            }

            String[] arr = imageName.split(":");
            if (arr.length < 2) {
                imageName += ":latest";
                configTable.setValueAt(imageName, 0, 1);
            }
        });
        configTable.addMouseMotionListener(new MouseAdapter(){
            public void mouseMoved(MouseEvent e) {
                int row=configTable.rowAtPoint(e.getPoint());
                int col=configTable.columnAtPoint(e.getPoint());
                if (row>-1 && col==0 && row < params.size()) {
                    String description = params.get(row).get("description");
                    configTable.setToolTipText(description);
                }
                else
                    configTable.setToolTipText(null);
            }
        });
        configTable.setModel(model);
        configTable.setCellSelectionEnabled(true);
    }

    public JTable getConfigTable() {
        return configTable;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (configTable.isEditing()) {
            TableCellEditor editor = configTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
        return super.doValidate();
    }
}
