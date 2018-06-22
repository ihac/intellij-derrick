package xyz.ihac.intellij.plugin.derrick.docker;

import java.io.Serializable;

public class DockerRegistryConfiguration implements Serializable {
    private String name;
    private String url;
    private String username;
    private String password;
    private String email;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public DockerRegistryConfiguration() {
        name = "##unknown##";
    }

    public DockerRegistryConfiguration(String name, String url, String username, String password, String email) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }
}
