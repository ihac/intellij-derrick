package xyz.ihac.intellij.plugin.derrick;

import java.io.Serializable;

public class K8sClusterConfiguration implements Serializable {
    private String name;
    private String kubeconfig;

    public K8sClusterConfiguration() {
        name = "##unknown##";
    }

    public K8sClusterConfiguration(String name, String kubeconfig) {
        this.name = name;
        this.kubeconfig = kubeconfig;
    }

    public String getName() {
        return name;
    }

    public String getKubeconfig() {
        return kubeconfig;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKubeconfig(String kubeconfig) {
        this.kubeconfig = kubeconfig;
    }

    @Override
    public String toString() {
        return name;
    }
}
