package xyz.ihac.intellij.plugin.derrick;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

@State(name="DerrickOptionProvider", storages={@Storage("derrick-app.xml")})
public class DerrickOptionProvider implements PersistentStateComponent<DerrickOptionProvider> {
    private String derrickExecPath;
    private String dockerExecPath;

    private LinkedList<DockerRegistryConfiguration> dockerRegistries;
    private LinkedList<K8sClusterConfiguration> k8sClusters;


    public DerrickOptionProvider() {
        derrickExecPath = "/usr/local/bin/derrick_mock";
        dockerExecPath = "unix:///var/run/docker.sock";
        k8sClusters = new LinkedList<>();
        dockerRegistries = new LinkedList<>();
    }

    @Nullable
    @Override
    public DerrickOptionProvider getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull DerrickOptionProvider state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public LinkedList<K8sClusterConfiguration> getK8sClusters() {
        return k8sClusters;
    }

    public LinkedList<DockerRegistryConfiguration> getDockerRegistries() {
        return dockerRegistries;
    }

    public String getDerrickExecPath() {
        return derrickExecPath;
    }

    public String getDockerExecPath() {
        return dockerExecPath;
    }


    public void setDerrickExecPath(String derrickExecPath) {
        this.derrickExecPath = derrickExecPath;
    }

    public void setDockerExecPath(String dockerExecPath) {
        this.dockerExecPath = dockerExecPath;
    }

    public void setK8sClusters(LinkedList<K8sClusterConfiguration> clusters) {
        this.k8sClusters = new LinkedList<>(clusters);
    }

    public void setDockerRegistries(LinkedList<DockerRegistryConfiguration> registries) {
        this.dockerRegistries = new LinkedList<>(registries);
    }
}

