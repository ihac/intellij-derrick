package xyz.ihac.intellij.plugin.derrick;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.ihac.intellij.plugin.derrick.ui.DerrickSettingsForm;

import javax.swing.*;

public class DerrickConfigurable implements SearchableConfigurable {
    private DerrickOptionProvider option;
    private DerrickProjectOptionProvider projOption;
    private DerrickSettingsForm settingsForm;

    public DerrickConfigurable(Project project) {
        option = ServiceManager.getService(DerrickOptionProvider.class);
        projOption = ServiceManager.getService(project, DerrickProjectOptionProvider.class);
    }

    @NotNull
    @Override
    public String getId() {
        return "preference.derrick";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Derrick";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsForm = new DerrickSettingsForm(option, projOption);
        return settingsForm.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return settingsForm!= null && projOption != null && (!Comparing.equal(settingsForm.getWorkDir(), projOption.getWorkDir()) ||
                !Comparing.equal(settingsForm.getDerrickExecPath(), option.getDerrickExecPath()) ||
                !Comparing.equal(settingsForm.getDockerExecPath(), option.getDockerExecPath()) ||
                !Comparing.equal(settingsForm.getRegistryAddress(), option.getRegistryAddress()) ||
                !Comparing.equal(settingsForm.getKubeConfigPath(), option.getKubeConfigPath()) ||
                !Comparing.equal(settingsForm.getUsername(), option.getUsername()) ||
                !Comparing.equal(settingsForm.getPassword(), option.getPassword())
        );
    }

    @Override
    public void apply() throws ConfigurationException {
        if (settingsForm != null && projOption != null) {
            projOption.setWorkDir(settingsForm.getWorkDir());
            option.setDerrickExecPath(settingsForm.getDerrickExecPath());
            option.setDockerExecPath(settingsForm.getDockerExecPath());
            option.setRegistryAddress(settingsForm.getRegistryAddress());
            option.setKubeConfigPath(settingsForm.getKubeConfigPath());
            option.setUsername(settingsForm.getUsername());
            option.setPassword(settingsForm.getPassword());
        }
    }
}
