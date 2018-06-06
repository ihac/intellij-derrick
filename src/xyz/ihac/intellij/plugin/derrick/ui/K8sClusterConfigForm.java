package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.DerrickProjectOptionProvider;

import javax.swing.*;
import java.io.File;

public class K8sClusterConfigForm extends DialogWrapper {
    private JPanel rootPanel;
    private JTextField k8sNameTextField;
    private JLabel k8sNameLabel;
    private JLabel kubeConfigPathLabel;
    private JPanel kubeConfigPathPanel;

    private TextFieldWithBrowseButton kubeConfigPathTextField;

    protected K8sClusterConfigForm(@Nullable Project project) {
        super(project);
        super.init();
        super.setTitle("Kubernetes Cluster Settings");

        initComponents();
        initDefaultValue(project);
    }

    public K8sClusterConfigForm(@Nullable Project project, String name, String kubeconfig) {
        this(project);

        if (name != null) {
            k8sNameTextField.setText(name);
        }
        if (kubeconfig != null) {
            kubeConfigPathTextField.setText(kubeconfig);
        }
    }

    private TextFieldWithBrowseButton createTextFieldWithBrowseButton(String browserTitle, Boolean chooseDir) {
        LabeledComponent<TextFieldWithBrowseButton> labeledComponent = new LabeledComponent<>();
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(browserTitle, "", null,
                new FileChooserDescriptor(!chooseDir, chooseDir, false, false, false, false));
        return textField;
    }

    private void initComponents() {
        /*
         * create text field with browse button for kubeconfig location.
         */
        kubeConfigPathTextField = createTextFieldWithBrowseButton("Select deployment YAML", false);
        kubeConfigPathPanel.setLayout(new BoxLayout(kubeConfigPathPanel, BoxLayout.X_AXIS));
        kubeConfigPathPanel.add(kubeConfigPathTextField);
    }

    private void initDefaultValue(@Nullable Project project) {
        /*
         * set default kubeconfig location.
         */
        if (project != null) {
            DerrickProjectOptionProvider projOption = ServiceManager.getService(project, DerrickProjectOptionProvider.class);
            if (projOption != null) {
                String deployYaml = projOption.getWorkDir() + "/.kubeconfig";
                File f = new File(deployYaml);
                if (f.exists() && !f.isDirectory())
                    kubeConfigPathTextField.setText(deployYaml);
            }
        }
    }

    public String getKubeConfigPath() {
        return kubeConfigPathTextField.getText().trim();
    }

    public String getClusterName() {
        return k8sNameTextField.getText().trim();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (getClusterName().equals(""))
            return new ValidationInfo("Please enter valid name of the cluster", k8sNameTextField);
        String path = getKubeConfigPath();
        if (path.equals("") || !new File(path).exists() || new File(path).isDirectory())
            return new ValidationInfo("Please enter valid kubeconfig path of the cluster", kubeConfigPathTextField);
        return super.doValidate();
    }
}
