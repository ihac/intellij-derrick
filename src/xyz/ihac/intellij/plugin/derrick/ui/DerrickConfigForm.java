package xyz.ihac.intellij.plugin.derrick.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DerrickConfigForm extends DialogWrapper {
    protected DerrickConfigForm(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }
}
