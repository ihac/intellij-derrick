package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import xyz.ihac.intellij.plugin.derrick.DerrickOptionProvider;
import xyz.ihac.intellij.plugin.derrick.DerrickProjectOptionProvider;
import xyz.ihac.intellij.plugin.derrick.docker.DockerRegistryConfiguration;
import xyz.ihac.intellij.plugin.derrick.kubernetes.K8sClusterConfiguration;
import xyz.ihac.intellij.plugin.derrick.kubernetes.AliyunCS;
import xyz.ihac.intellij.plugin.derrick.derrick.Derrick;
import xyz.ihac.intellij.plugin.derrick.kubernetes.AliyunCSClusterConfiguration;
import xyz.ihac.intellij.plugin.derrick.util.DerrickIcon;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class DerrickSettingsForm {
    private JPanel rootPanel;
    private JPanel projectSettingPanel;
    private JPanel applicationSettingPanel;
    private JPanel derrickSettingPanel;
    private JPanel dockerSettingPanel;
    private JPanel kubernetesSettingPanel;
    private JPanel dockerExecPathPanel;
    private JLabel dockerExecLabel;
    private JButton testButton;
    private JPanel derrickExecPathPanel;
    private JPanel workDirPathPanel;
    private JLabel derrickExecPathTestLabel;
    private JPanel dockerRegistrySettingPanel;

    private TextFieldWithBrowseButton workDirPathTextField;
    private TextFieldWithBrowseButton derrickExecPathTextField;
    private TextFieldWithBrowseButton dockerExecPathTextField;

    private LinkedList<K8sClusterConfiguration> k8sClusters;
    private JBList k8sClusterList;
    private LinkedList<DockerRegistryConfiguration> dockerRegistries;
    private JBList dockerRegistryList;

    private Project project;


    public DerrickSettingsForm(Project project) {
        this.project = project;

        initComponents();
        initBorder();
        initDefaultValue(project);
    }

    private void initDefaultValue(Project project) {
        DerrickOptionProvider option = ServiceManager.getService(DerrickOptionProvider.class);
        DerrickProjectOptionProvider projOption = ServiceManager.getService(project, DerrickProjectOptionProvider.class);

        if (projOption != null) {
            workDirPathTextField.setText(projOption.getWorkDir());
        }
        if (option != null) {
            CollectionListModel<K8sClusterConfiguration> listModel = new CollectionListModel<>();
            k8sClusterList.setModel(listModel);
            k8sClusters = new LinkedList<>(option.getK8sClusters());
            for (K8sClusterConfiguration cluster: k8sClusters) {
                listModel.add(cluster);
            }

            CollectionListModel<DockerRegistryConfiguration> listModel2 = new CollectionListModel<>();
            dockerRegistryList.setModel(listModel2);
            dockerRegistries = new LinkedList<>(option.getDockerRegistries());
            for (DockerRegistryConfiguration registry: dockerRegistries) {
                listModel2.add(registry);
            }

            derrickExecPathTextField.setText(option.getDerrickExecPath());
            dockerExecPathTextField.setText(option.getDockerExecPath());
        }
    }

    private void initComponents() {
        dockerRegistryList = new JBList();
        dockerRegistryList.getEmptyText().setText("No docker registry configured");
        ToolbarDecorator dockerDecorator = ToolbarDecorator.createDecorator(dockerRegistryList).disableUpDownActions();
        dockerDecorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                DockerRegistryConfigForm dialog = new DockerRegistryConfigForm(project);
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    DockerRegistryConfiguration newRegistry = new DockerRegistryConfiguration(
                            dialog.getName(),
                            dialog.getRegistryUrl(),
                            dialog.getUsername(),
                            dialog.getPassword(),
                            dialog.getEmail());
                    dockerRegistries.add(newRegistry);
                    ((CollectionListModel<DockerRegistryConfiguration>) dockerRegistryList.getModel()).add(newRegistry);
                }
            }
        }).setAddActionName("Add")
        .setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                DockerRegistryConfiguration registry = (DockerRegistryConfiguration) dockerRegistryList.getSelectedValue();
                dockerRegistries.remove(registry);
                ((CollectionListModel<DockerRegistryConfiguration>) dockerRegistryList.getModel()).remove(registry);

            }
        }).setRemoveActionName("Remove")
        .setEditAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                int index = dockerRegistryList.getSelectedIndex();
                DockerRegistryConfiguration registry = dockerRegistries.get(index);

                DockerRegistryConfigForm dialog = new DockerRegistryConfigForm(
                        project,
                        registry.getName(),
                        registry.getUrl(),
                        registry.getUsername(),
                        registry.getPassword(),
                        registry.getEmail());
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    DockerRegistryConfiguration newRegistry = new DockerRegistryConfiguration(
                            dialog.getName(),
                            dialog.getRegistryUrl(),
                            dialog.getUsername(),
                            dialog.getPassword(),
                            dialog.getEmail());
                    dockerRegistries.set(index, newRegistry);
                    ((CollectionListModel<DockerRegistryConfiguration>) dockerRegistryList.getModel()).setElementAt(newRegistry, index);
                }
            }
        }).setEditActionName("Edit");

        dockerRegistryList.setCellRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
                DockerRegistryConfiguration registry = (DockerRegistryConfiguration) value;
                append(registry.toString());
                setIcon(DerrickIcon.TOOL_DOCKER());
                if (!selected && index % 2 == 0) {
                    setBackground(UIUtil.getDecoratedRowColor());
                }
            }
        });
        dockerDecorator.setPreferredSize(new Dimension(-1, 100));
        dockerRegistrySettingPanel.setLayout(new BoxLayout(dockerRegistrySettingPanel, BoxLayout.Y_AXIS));
        dockerRegistrySettingPanel.add(dockerDecorator.createPanel());

        k8sClusterList = new JBList();
        k8sClusterList.getEmptyText().setText("No kubernetes cluster configured");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(k8sClusterList).disableUpDownActions();
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                K8sClusterConfigForm dialog = new K8sClusterConfigForm(project);
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    K8sClusterConfiguration newCluster = new K8sClusterConfiguration(
                            K8sClusterConfiguration.STANDARD_K8S_CLUSTER,
                            dialog.getClusterName(),
                            dialog.getKubeConfigPath());
                    k8sClusters.add(newCluster);
                    ((CollectionListModel<K8sClusterConfiguration>) k8sClusterList.getModel()).add(newCluster);
                }
            }
        }).setAddActionName("Add")
        .setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
               K8sClusterConfiguration cluster = (K8sClusterConfiguration) k8sClusterList.getSelectedValue();
               k8sClusters.remove(cluster);
               ((CollectionListModel<K8sClusterConfiguration>) k8sClusterList.getModel()).remove(cluster);
            }
        }).setRemoveActionName("Remove")
        .setEditAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                int index = k8sClusterList.getSelectedIndex();
                K8sClusterConfiguration cluster = k8sClusters.get(index);
                K8sClusterConfigForm dialog = new K8sClusterConfigForm(
                        project,
                        cluster.getCtype(),
                        cluster.getName(),
                        cluster.getKubeconfig());
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    K8sClusterConfiguration newCluster;
                    if (cluster.getCtype() == K8sClusterConfiguration.ALIYUN_CS_CLUSTER) {
                        newCluster = new K8sClusterConfiguration(
                                K8sClusterConfiguration.ALIYUN_CS_CLUSTER,
                                dialog.getClusterName(),
                                cluster.getKubeconfig()
                        );
                    }
                    else {
                        newCluster = new K8sClusterConfiguration(
                                K8sClusterConfiguration.STANDARD_K8S_CLUSTER,
                                dialog.getClusterName(),
                                dialog.getKubeConfigPath()
                        );
                    }
                    k8sClusters.set(index, newCluster);
                    ((CollectionListModel<K8sClusterConfiguration>) k8sClusterList.getModel()).setElementAt(newCluster, index);
                }
            }
        }).setEditActionName("Edit")
        .addExtraAction(new AnActionButton("Add Aliyun CS Cluster", DerrickIcon.ACTION_ADD_ALIYUN()) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                AliyunCSConfigForm dialog = new AliyunCSConfigForm(project, false);
                dialog.show();
                if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    AliyunCS client = new AliyunCS(
                            dialog.getRegion(),
                            dialog.getAccessKeyID(),
                            dialog.getAccessKeySecret());

                    java.util.List<AliyunCSClusterConfiguration> allClusters = client.describeClusters();
                    dialog = new AliyunCSConfigForm(project, allClusters);
                    dialog.show();
                    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        java.util.List<AliyunCSClusterConfiguration> clusters = dialog.getSelectedClusters();
                        if (clusters.size() != 0) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    k8sClusterList.setPaintBusy(true);
                                    k8sClusterList.setToolTipText("downloading certificates of your clusters");
                                    for (AliyunCSClusterConfiguration cluster : clusters) {
                                        CollectionListModel<K8sClusterConfiguration> model =
                                                (CollectionListModel<K8sClusterConfiguration>) k8sClusterList.getModel();

                                        String kubeConfigContent = client.describeClusterKubeConfig(cluster.id());
                                        K8sClusterConfiguration newCluster = new K8sClusterConfiguration(
                                                K8sClusterConfiguration.ALIYUN_CS_CLUSTER,
                                                cluster.name(),
                                                kubeConfigContent);
                                        model.add(newCluster);
                                        k8sClusters.add(newCluster);
                                    }
                                    k8sClusterList.setPaintBusy(false);
                                    k8sClusterList.setToolTipText(null);
                                }
                            }).start();
                        }
                    }
                }
            }
        }).setButtonComparator("Add", "Add Aliyun CS Cluster", "Remove", "Edit");

        k8sClusterList.setCellRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
                K8sClusterConfiguration cluster = (K8sClusterConfiguration) value;
                append(cluster.toString());
                if (cluster.getCtype() == K8sClusterConfiguration.ALIYUN_CS_CLUSTER) {
                    setIcon(DerrickIcon.TOOL_ALIYUN());
                }
                else {
                    setIcon(DerrickIcon.TOOL_KUBERNETES());
                }
                if (!selected && index % 2 == 0) {
                    setBackground(UIUtil.getDecoratedRowColor());
                }
            }
        });
        decorator.setPreferredSize(new Dimension(-1, 100));
        kubernetesSettingPanel.setLayout(new BoxLayout(kubernetesSettingPanel, BoxLayout.Y_AXIS));
        kubernetesSettingPanel.add(decorator.createPanel());

        workDirPathTextField = createTextFieldWithBrowseButton("Select work directory", true);
        workDirPathPanel.setLayout(new BoxLayout(workDirPathPanel, BoxLayout.X_AXIS));
        workDirPathPanel.add(workDirPathTextField);

        derrickExecPathTextField = createTextFieldWithBrowseButton("Select derrick executable", false);
        derrickExecPathPanel.setLayout(new BoxLayout(derrickExecPathPanel, BoxLayout.X_AXIS));
        derrickExecPathPanel.add(derrickExecPathTextField);

        dockerExecPathTextField = createTextFieldWithBrowseButton("Select docker executable", false);
        dockerExecPathPanel.setLayout(new BoxLayout(dockerExecPathPanel, BoxLayout.X_AXIS));
        dockerExecPathPanel.add(dockerExecPathTextField);

        derrickExecPathTestLabel.setVisible(false);
        testButton.addActionListener(e -> {
            derrickExecPathTestLabel.setVisible(true);
            String derrickComm = getDerrickExecPath();
            if (derrickComm != null && Derrick.verify(derrickComm, workDirPathTextField.getText().trim()))
                derrickExecPathTestLabel.setIcon(DerrickIcon.STATE_SUCCESS());
            else
                derrickExecPathTestLabel.setIcon(DerrickIcon.STATE_FAIL());
        });
    }

    private void initBorder() {
        projectSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Project settings"));
        applicationSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Application settings"));

        derrickSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Derrick"));
        dockerSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Docker"));
        kubernetesSettingPanel.setBorder(IdeBorderFactory.createTitledBorder("Kubernetes"));
    }

    private TextFieldWithBrowseButton createTextFieldWithBrowseButton(String browserTitle, Boolean chooseDir) {
        LabeledComponent<TextFieldWithBrowseButton> labeledComponent = new LabeledComponent<>();
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(browserTitle, "", null,
                new FileChooserDescriptor(!chooseDir, chooseDir, false, false, false, false));
        return textField;
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    public String getWorkDir() {
        return workDirPathTextField.getText().trim();
    }

    public String getDerrickExecPath() {
        return derrickExecPathTextField.getText().trim();
    }

    public String getDockerExecPath() {
        return dockerExecPathTextField.getText().trim();
    }

    public LinkedList<K8sClusterConfiguration> getK8sClusters() {
        return k8sClusters;
    }

    public LinkedList<DockerRegistryConfiguration> getDockerRegistries() {
        return dockerRegistries;
    }
}
