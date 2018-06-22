package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.DerrickOptionProvider;
import xyz.ihac.intellij.plugin.derrick.docker.DockerRegistryConfiguration;
import xyz.ihac.intellij.plugin.derrick.kubernetes.K8sClusterConfiguration;
import xyz.ihac.intellij.plugin.derrick.ui.model.DerrickConfigTableModel;
import xyz.ihac.intellij.plugin.derrick.util.ImageNameFormat;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class DerrickConfigForm extends DialogWrapper {
    private JPanel rootPanel;

    /** Represents the basic configuration for this action */
    private JPanel basicConfigPanel;
    private JPanel rebuildPanel;
    private JCheckBox reBuildCheckBox;
    private JPanel imageNamePanel;
    private JLabel imageNameLabel;
    private JTextField imageNameTextField;
    private JPanel registryPanel;
    private JLabel registryLabel;
    private JComboBox registryComboBox;
    private JPanel deployConfigPanel;
    private JLabel kubernetesClusterLabel;
    private JComboBox kubernetesClusterComboBox;
    private JPanel deploymentYamlPathPanel;
    private JLabel deploymentYamlLabel;
    private TextFieldWithBrowseButton deploymentYamlTextField;
    private JPanel derrickRiggingPanel;
    private JLabel derrickRiggingLabel;
    private JComboBox derrickRiggingComboBox;

    /** Represents the configuration required by Derrick */
    private JPanel derrickConfigPanel;
    private JBTable derrickConfigTable;

    private String action;

    public DerrickConfigForm(@Nullable Project project,
                             String action) {
        super(project);
        super.init();
        super.setTitle("%s Config".format(action));

        this.action = action;

        setupComponents(project);
    }

    public DerrickConfigForm(@Nullable Project project,
                             String action,
                             Map<String, List<Map<String, String>>> riggingsAndParams) {
        this(project, action);

        addRiggings(riggingsAndParams);
    }

    private void setupComponents(@Nullable Project project) {
        /** Sets visibility based on action */
        if (action.equals("Init")) {
            rebuildPanel.setVisible(false);
            imageNamePanel.setVisible(false);
            deployConfigPanel.setVisible(false);
        }
        else if (action.equals("Serve")) {
            registryPanel.setVisible(false);
            deployConfigPanel.setVisible(false);
            derrickRiggingPanel.setVisible(false);
            derrickConfigPanel.setVisible(false);
        }
        else if (action.equals("Push")) {
            registryPanel.setVisible(false);
            deployConfigPanel.setVisible(false);
            derrickRiggingPanel.setVisible(false);
            derrickConfigPanel.setVisible(false);
        }
        else if (action.equals("Deploy")) {
            rebuildPanel.setVisible(false);
            imageNamePanel.setVisible(false);
            registryPanel.setVisible(false);
            derrickRiggingPanel.setVisible(false);
            derrickConfigPanel.setVisible(false);
        }

        DerrickOptionProvider option = ServiceManager.getService(DerrickOptionProvider.class);

        if (deployConfigPanel.isVisible()) {
            /** Creates a textfield with browse button and sets default value */
            deploymentYamlTextField = createTextFieldWithBrowseButton("Select deployment YAML", false);
            deploymentYamlPathPanel.setLayout(new BoxLayout(deploymentYamlPathPanel, BoxLayout.X_AXIS));
            deploymentYamlPathPanel.add(deploymentYamlTextField);
            if (project != null) {
                deploymentYamlTextField.setText(project.getBasePath() + "/" + "kubernetes-deployment.yaml");
            }

            /** Adds predefined kubernetes clusters */
            for (K8sClusterConfiguration cluster: option.getK8sClusters()) {
                kubernetesClusterComboBox.addItem(cluster);
            }
        }

        if (rebuildPanel.isVisible()) {
            /** Adds listener for rebuild image checkbox */
            addReBuildCheckBoxListener();
        }

        if (registryPanel.isVisible()) {
            /** Adds predefined docker registries */
            for (DockerRegistryConfiguration registry: option.getDockerRegistries()) {
                registryComboBox.addItem(registry);
            }

            registryComboBox.addActionListener(e -> {
                DerrickConfigTableModel model = (DerrickConfigTableModel) derrickConfigTable.getModel();

                int imageNameRow = model.getImageNameRow();
                // only work when 'image name' config is present.
                if (imageNameRow != -1) {
                    String imageName = (String) model.getValueAt(imageNameRow, 1);
                    DockerRegistryConfiguration registry = (DockerRegistryConfiguration) registryComboBox.getSelectedItem();

                    String newName = autoCorrectImageName(imageName, registry);
                    if (!newName.equals(imageName)) {
                        model.setValueAt(autoCorrectImageName(imageName, registry),
                                imageNameRow, 1);
                    }
                }
            });
        }

        if (derrickConfigPanel.isVisible()) {
            /** Creates a table for derrick config */
            derrickConfigTable = new JBTable(new DerrickConfigTableModel());

            derrickConfigTable.addMouseMotionListener(new MouseAdapter(){
                public void mouseMoved(MouseEvent e) {
                    int row= derrickConfigTable.rowAtPoint(e.getPoint());
                    DerrickConfigTableModel model = (DerrickConfigTableModel) derrickConfigTable.getModel();
                    List<Map<String, String>> params = model.getRawParams();

                    if (row > -1 && row < params.size()) {
                        String description = params.get(row).get("description");
                        derrickConfigTable.setToolTipText(description);
                    }
                    else
                        derrickConfigTable.setToolTipText(null);
                }
            });
            derrickConfigTable.setCellSelectionEnabled(true);
            derrickConfigTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            derrickConfigTable.setStriped(true);

            JBScrollPane scrollPane = new JBScrollPane(derrickConfigTable);
            scrollPane.setPreferredSize(new Dimension(-1, 150));
            derrickConfigPanel.setLayout(new BoxLayout(derrickConfigPanel, BoxLayout.Y_AXIS));
            derrickConfigPanel.add(scrollPane);
        }
    }

    private void addRiggings(Map<String, List<Map<String, String>>> riggingsAndParams) {
        derrickRiggingComboBox.addActionListener(e -> {
            String rigging = (String) derrickRiggingComboBox.getSelectedItem();
            updateTableContents(riggingsAndParams.get(rigging));
        });
        for (String rigging: riggingsAndParams.keySet()) {
            derrickRiggingComboBox.addItem(rigging);
        }
    }

    private void updateTableContents(List<Map<String, String>> params) {
        DerrickConfigTableModel model = new DerrickConfigTableModel(params);
        model.addTableModelListener(e -> {
            int imageNameRow = model.getImageNameRow();
            // only work when 'image name' config is present.
            if (imageNameRow != -1) {
                if (e.getColumn() != 1 || e.getLastRow() != imageNameRow) return;

                String imageName = (String) model.getValueAt(imageNameRow, 1);
                DockerRegistryConfiguration registry = (DockerRegistryConfiguration) registryComboBox.getSelectedItem();

                String newName = autoCorrectImageName(imageName, registry);
                if (!newName.equals(imageName)) {
                    model.setValueAt(autoCorrectImageName(imageName, registry),
                            imageNameRow, 1);
                }
            }
        });
        derrickConfigTable.setModel(model);
    }

    private String autoCorrectImageName(String imageName, DockerRegistryConfiguration registry) {
        String[] imageNameArr = imageName.split("/");

        String username = registry.getUsername();
        if (imageNameArr.length < 2) {
            imageName = username + "/" + imageName;
        }
        else {
            imageName = username + "/" + imageNameArr[1];
        }

        String[] arr = imageName.split(":");
        if (arr.length < 2) {
            imageName += ":latest";
        }
        return imageName;
    }

    private void addReBuildCheckBoxListener() {
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

    ////////// Getters //////////

    public Boolean getIsRebuild() {
        return reBuildCheckBox.isSelected();
    }

    public String getRigging() {
        return (String) derrickRiggingComboBox.getSelectedItem();
    }

    public Map<String, String> getParams() {
        DerrickConfigTableModel model = (DerrickConfigTableModel) derrickConfigTable.getModel();
        return model.getParams();
    }

    public String getDeploymentYaml() {
        return deploymentYamlTextField.getText();
    }

    public String getImageName() {
        return imageNameTextField.getText();
    }

    public DockerRegistryConfiguration getDockerRegistry() {
        return (DockerRegistryConfiguration) registryComboBox.getSelectedItem();
    }

    public K8sClusterConfiguration getK8sCluster() {
        return (K8sClusterConfiguration) kubernetesClusterComboBox.getSelectedItem();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (action.equals("Init")) {
            if (derrickConfigTable.isEditing()) {
                TableCellEditor editor = derrickConfigTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }
            }
            DerrickConfigTableModel model = (DerrickConfigTableModel) derrickConfigTable.getModel();
            int row = model.getImageNameRow();
            if (row >= 0) {
                String imageName = (String) derrickConfigTable.getValueAt(row, 1);
                if (!ImageNameFormat.verify(imageName)) {
                    return new ValidationInfo("Please enter valid image name", derrickConfigTable);
                }
            }
        }
        return super.doValidate();
    }
}
