package xyz.ihac.intellij.plugin.derrick.kubernetes;

import java.io.Serializable;

public class K8sClusterConfiguration implements Serializable {
    public static final int ALIYUN_CS_CLUSTER = 0;
    public static final int STANDARD_K8S_CLUSTER = 1;

    private int ctype;
    private String name;
    private String kubeconfig;

    public K8sClusterConfiguration() {
        name = "##unknown##";
    }

    public K8sClusterConfiguration(int ctype, String name, String kubeconfig) {
        assert(ctype == ALIYUN_CS_CLUSTER || ctype == STANDARD_K8S_CLUSTER);
        this.ctype = ctype;
        this.name = name;
        this.kubeconfig = kubeconfig;
    }

    public int getCtype() {
        return ctype;
    }

    public String getName() {
        return name;
    }

    public String getKubeconfig() {
        return kubeconfig;
    }

    public void setCtype(int ctype) {
        this.ctype = ctype;
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
