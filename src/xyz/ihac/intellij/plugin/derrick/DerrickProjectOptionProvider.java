package xyz.ihac.intellij.plugin.derrick;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name="DerrickProjectOptionProvider", storages={@Storage("derrick.xml")})
public class DerrickProjectOptionProvider implements PersistentStateComponent<DerrickProjectOptionProvider> {
    private String workDir;

    @Nullable
    @Override
    public DerrickProjectOptionProvider getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull DerrickProjectOptionProvider state) {
        workDir = state.getWorkDir();
    }

    public DerrickProjectOptionProvider(Project project) {
        workDir = project.getBasePath();
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
}
