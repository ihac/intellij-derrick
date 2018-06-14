package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.DerrickOptionProvider;
import xyz.ihac.intellij.plugin.derrick.DerrickProjectOptionProvider;
import xyz.ihac.intellij.plugin.derrick.K8sClusterConfiguration;
import xyz.ihac.intellij.plugin.derrick.util.NonEmpty;
import xyz.ihac.intellij.plugin.derrick.util.NotEqual;

import javax.swing.*;
import java.io.File;

import static xyz.ihac.intellij.plugin.derrick.K8sClusterConfiguration.ALIYUN_CS_CLUSTER;
import static xyz.ihac.intellij.plugin.derrick.K8sClusterConfiguration.STANDARD_K8S_CLUSTER;

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

    public K8sClusterConfigForm(@Nullable Project project, int ctype, String name, String kubeconfig) {
        this(project);

        if (name != null) {
            k8sNameTextField.setText(name);
        }
        switch (ctype) {
            case ALIYUN_CS_CLUSTER: {
                kubeConfigPathTextField.setText("");
                kubeConfigPathTextField.setEnabled(false);
                kubeConfigPathLabel.setEnabled(false);
                break;
            }
            case STANDARD_K8S_CLUSTER: {
                if (kubeconfig != null) {
                    kubeConfigPathTextField.setText(kubeconfig);
                }
                break;
            }
            default:
                throw new IllegalArgumentException(
                        String.format("Unknown cluster type: %d when creating K8sClusterConfig Dialog", ctype));
        }
    }

    private TextFieldWithBrowseButton createTextFieldWithBrowseButton(String browserTitle, Boolean chooseDir) {
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
        if (!NonEmpty.verify(getClusterName()))
            return new ValidationInfo("Please enter valid name of the cluster", k8sNameTextField);

        // TODO: Clusters which are not applied should be considered.
        DerrickOptionProvider option = ServiceManager.getService(DerrickOptionProvider.class);
        for (K8sClusterConfiguration cluster: option.getK8sClusters()) {
            if (!NotEqual.set(cluster.getName()).verify(getClusterName()))
                return new ValidationInfo("Cluster name conflicts with existing clusters", k8sNameTextField);
        }

        String path = getKubeConfigPath();
        if (!NonEmpty.verify(path) || !new File(path).exists() || new File(path).isDirectory())
            return new ValidationInfo("Please enter valid kubeconfig path of the cluster", kubeConfigPathTextField);

        return super.doValidate();
    }
}
