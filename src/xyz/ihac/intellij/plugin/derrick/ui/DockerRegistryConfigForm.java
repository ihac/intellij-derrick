package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.DockerRegistryConfiguration;

import javax.swing.*;

public class DockerRegistryConfigForm extends DialogWrapper {
    private JPanel rootPanel;
    private JPanel dockerSettingPanel;
    private JLabel registryAddressLabel;
    private JLabel usernameLabel;
    private JTextField usernameTextField;
    private JLabel passwordLabel;
    private JPasswordField passwordTextField;
    private JComboBox registryAddressBox;
    private JLabel emailLabel;
    private JTextField emailTextField;
    private JTextField registryNameTextField;
    private JLabel registryNameLabel;

    protected DockerRegistryConfigForm(@Nullable Project project) {
        super(project);
        super.init();
        super.setTitle("Docker Registry Settings");

        initDefaultValue();
    }

    public DockerRegistryConfigForm(@Nullable Project project,
                                       String name,
                                       String url,
                                       String username,
                                       String password,
                                       String email) {
        this(project);

        registryNameTextField.setText(name);
        registryAddressBox.setSelectedItem(url);
        usernameTextField.setText(username);
        passwordTextField.setText(password);
        emailTextField.setText(email);
    }

    private void initDefaultValue() {
        registryAddressBox.addItem("registry.hub.docker.com");
        registryAddressBox.addItem("registry.cn-hangzhou.aliyuncs.com");
        registryAddressBox.setSelectedItem("registry.hub.docker.com");
    }

    public String getName() {
        return registryNameTextField.getText().trim();
    }

    public String getRegistryUrl() {
        return String.valueOf(registryAddressBox.getSelectedItem()).trim();
    }

    public String getUsername() {
        return usernameTextField.getText().trim();
    }

    public String getPassword() {
        return String.valueOf(passwordTextField.getPassword()).trim();
    }

    public String getEmail() {
        return emailTextField.getText().trim();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (getName().equals(""))
            return new ValidationInfo("Please enter valid name of the registry", registryNameTextField);
        if (getRegistryUrl().equals(""))
            return new ValidationInfo("Please enter valid url of the registry", registryAddressBox);
        if (getUsername().equals(""))
            return new ValidationInfo("Please enter valid username of the registry", usernameTextField);
        if (getPassword().equals(""))
            return new ValidationInfo("Please enter valid password of the registry", passwordTextField);
        if (getEmail().equals(""))
            return new ValidationInfo("Please enter valid email of the registry", emailTextField);
        return super.doValidate();
    }
}
