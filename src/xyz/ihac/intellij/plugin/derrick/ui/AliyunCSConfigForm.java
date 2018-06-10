package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.util.NonEmpty;

import javax.swing.*;

public class AliyunCSConfigForm extends DialogWrapper {
    private JPanel rootPanel;
    private JTextField accessKeyIDTextField;
    private JPasswordField accessKeySecretPasswdField;
    private JTextField regionTextField;
    private JLabel accessKeyIDLabel;
    private JLabel accessKeySecretLabel;
    private JLabel regionLabel;

    public AliyunCSConfigForm(@Nullable Project project) {
        super(project);
        super.init();
        super.setTitle("Aliyun Container Service Settings");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (!NonEmpty.verify(getRegion()))
            return new ValidationInfo("Please enter valid region", regionTextField);
        if (!NonEmpty.verify(getAccessKeyID()))
            return new ValidationInfo("Please enter valid access key ID", accessKeyIDTextField);
        if (!NonEmpty.verify(getAccessKeySecret()))
            return new ValidationInfo("Please enter valid access key secret", accessKeySecretPasswdField);
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
}
