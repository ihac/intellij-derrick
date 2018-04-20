package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IdeBorderFactory;
import xyz.ihac.intellij.plugin.derrick.DerrickOptionProvider;
import xyz.ihac.intellij.plugin.derrick.DerrickProjectOptionProvider;

import javax.swing.*;

public class DerrickSettingsForm {
    private JPanel rootPanel;
    private JPanel projectSettingPanel;
    private JPanel applicationSettingPanel;
    private JPanel derrickSettingPanel;
    private JPanel dockerSettingPanel;
    private JPanel kubernetesSettingPanel;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JComboBox registryAddressBox;
    private JPanel dockerExecPathPanel;
    private JLabel dockerExecLabel;
    private JLabel passwordLabel;
    private JLabel usernameLabel;
    private JLabel registryAddressLabel;
    private JButton testButton;
    private JPanel derrickExecPathPanel;
    private JPanel workDirPathPanel;
    private JLabel derrickExecPathTestLabel;

    private TextFieldWithBrowseButton workDirPathTextField;
    private TextFieldWithBrowseButton derrickExecPathTextField;
    private TextFieldWithBrowseButton dockerExecPathTextField;

    public DerrickSettingsForm(DerrickOptionProvider option, DerrickProjectOptionProvider projOption) {
        initComponents();
        initBorder();
        initDefaultValue(option, projOption);
    }

    private void initDefaultValue(DerrickOptionProvider option, DerrickProjectOptionProvider projOption) {
        if (projOption != null) {
            workDirPathTextField.setText(projOption.getWorkDir());
        }
        if (option != null) {
            derrickExecPathTextField.setText(option.getDerrickExecPath());
            dockerExecPathTextField.setText(option.getDockerExecPath());
            if (option.getRegistryAddress() != null)
                registryAddressBox.setSelectedItem(option.getRegistryAddress());
            else
                registryAddressBox.setSelectedItem("registry.hub.docker.com");
            usernameTextField.setText(option.getUsername());
            passwordTextField.setText(option.getPassword());
        }
    }

    private void initComponents() {
        workDirPathTextField = createTextFieldWithBrowseButton("Select work directory", true);
        workDirPathPanel.setLayout(new BoxLayout(workDirPathPanel, BoxLayout.X_AXIS));
        workDirPathPanel.add(workDirPathTextField);

        derrickExecPathTextField = createTextFieldWithBrowseButton("Select derrick executable", false);
        derrickExecPathPanel.setLayout(new BoxLayout(derrickExecPathPanel, BoxLayout.X_AXIS));
        derrickExecPathPanel.add(derrickExecPathTextField);

        dockerExecPathTextField = createTextFieldWithBrowseButton("Select docker executable", false);
        dockerExecPathPanel.setLayout(new BoxLayout(dockerExecPathPanel, BoxLayout.X_AXIS));
        dockerExecPathPanel.add(dockerExecPathTextField);

        registryAddressBox.setEditable(true);

        derrickExecPathTestLabel.setVisible(false);
        testButton.addActionListener(e -> {
            derrickExecPathTestLabel.setVisible(true);
            String derrickComm = getDerrickExecPath();
            if (derrickComm != null && derrickComm.equals("/usr/local/bin/derrick_demo"))
                derrickExecPathTestLabel.setIcon(IconLoader.findIcon("/icons/success_16x16.png"));
            else
                derrickExecPathTestLabel.setIcon(IconLoader.findIcon("/icons/fail_16x16.png"));
        });
    }

    private void initBorder() {
        projectSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Project settings"));
        applicationSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Application settings"));

        derrickSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Derrick"));
        dockerSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Docker"));
        kubernetesSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Kubernetes"));
    }

    private TextFieldWithBrowseButton createTextFieldWithBrowseButton(String browserTitle, Boolean chooseDir) {
        LabeledComponent<TextFieldWithBrowseButton> labeledComponent = new LabeledComponent<>();
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(browserTitle, "", null,
                new FileChooserDescriptor(!chooseDir, chooseDir, false, false, false, false));
        return textField;
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public String getWorkDir() {
        return workDirPathTextField.getText().trim();
    }

    public String getDerrickExecPath() {
        return derrickExecPathTextField.getText().trim();
    }

    public String getDockerExecPath() {
        return dockerExecPathTextField.getText().trim();
    }

    public String getRegistryAddress() {
        return String.valueOf(registryAddressBox.getSelectedItem()).trim();
    }

    public String getUsername() {
        return usernameTextField.getText().trim();
    }

    public String getPassword() {
        return String.valueOf(passwordTextField.getPassword()).trim();
    }
}
