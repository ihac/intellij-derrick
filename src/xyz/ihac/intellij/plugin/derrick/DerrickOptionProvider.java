package xyz.ihac.intellij.plugin.derrick;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name="DerrickOptionProvider", storages={@Storage("derrick.xml")})
public class DerrickOptionProvider implements PersistentStateComponent<DerrickOptionProvider> {
    private String derrickExecPath;
    private String dockerExecPath;
    private String registryAddress;
    private String username;
    private String password;

    public DerrickOptionProvider() {
    }

    @Nullable
    @Override
    public DerrickOptionProvider getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull DerrickOptionProvider state) {
        derrickExecPath = state.getDerrickExecPath();
        dockerExecPath = state.getDockerExecPath();
        registryAddress = state.getRegistryAddress();
        username = state.getUsername();
        password = state.getPassword();
    }

    public String getDerrickExecPath() {
        return derrickExecPath;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public String getDockerExecPath() {
        return dockerExecPath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setDerrickExecPath(String derrickExecPath) {
        this.derrickExecPath = derrickExecPath;
    }

    public void setDockerExecPath(String dockerExecPath) {
        this.dockerExecPath = dockerExecPath;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
