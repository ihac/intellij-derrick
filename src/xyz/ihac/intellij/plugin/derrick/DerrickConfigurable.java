package xyz.ihac.intellij.plugin.derrick;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
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
    private Project project;
    private DerrickOptionProvider option;
    private DerrickProjectOptionProvider projOption;
    private DerrickSettingsForm settingsForm;

    public DerrickConfigurable(Project project) {
        this.project = project;
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
        settingsForm = new DerrickSettingsForm(project);
        return settingsForm.getRootPanel();
    }

    @Override
    public boolean isModified() {
//        if (!Comparing.equal(settingsForm.getWorkDir(), projOption.getWorkDir()))
//            System.out.println("workDir: " + projOption.getWorkDir());
//        if (!Comparing.equal(settingsForm.getDerrickExecPath(), option.getDerrickExecPath()))
//            System.out.println("DerrickExecPath: " + option.getDerrickExecPath());
//        if (!Comparing.equal(settingsForm.getDockerExecPath(), option.getDockerExecPath()))
//            System.out.println("DockerExecPath: " + option.getDockerExecPath());
//        if (!Comparing.equal(settingsForm.getK8sClusters(), option.getK8sClusters()))
//            System.out.println("K8sCluster: " + option.getK8sClusters());
//        if (!Comparing.equal(settingsForm.getDockerRegistries(), option.getDockerRegistries()))
//            System.out.println("DockerRegistry: " + option.getDockerRegistries());
        return settingsForm!= null && projOption != null && (!Comparing.equal(settingsForm.getWorkDir(), projOption.getWorkDir()) ||
                !Comparing.equal(settingsForm.getDerrickExecPath(), option.getDerrickExecPath()) ||
                !Comparing.equal(settingsForm.getDockerExecPath(), option.getDockerExecPath()) ||
                !Comparing.equal(settingsForm.getDockerRegistries(), option.getDockerRegistries()) ||
                !Comparing.equal(settingsForm.getK8sClusters(), option.getK8sClusters())
        );
    }

    @Override
    public void apply() throws ConfigurationException {
        if (settingsForm != null && projOption != null) {
            projOption.setWorkDir(settingsForm.getWorkDir());
            option.setDerrickExecPath(settingsForm.getDerrickExecPath());
            option.setDockerExecPath(settingsForm.getDockerExecPath());
            option.setDockerRegistries(settingsForm.getDockerRegistries());
            option.setK8sClusters(settingsForm.getK8sClusters());
        }
    }
}
