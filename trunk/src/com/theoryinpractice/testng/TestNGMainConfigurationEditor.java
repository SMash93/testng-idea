/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 14/07/2006
 * Time: 20:01:09
 */
package com.theoryinpractice.testng;

import javax.swing.*;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TestNGMainConfigurationEditor extends SettingsEditor<TestNGConfiguration>
{
    //private static final Logger LOGGER = Logger.getInstance("TestNG Runner");
    private TestNGConfigurationEditor editor;
    private TestNGCoverageConfigurationEditor coverageEditor;
    //private SettingsEditorGroup<TestNGConfiguration> group;

    public TestNGMainConfigurationEditor(Project project) {
        editor = new TestNGConfigurationEditor(project);
        coverageEditor = new TestNGCoverageConfigurationEditor(project);

        //group = new SettingsEditorGroup<TestNGConfiguration>();
        //group.addEditor("Configuration", editor);
        //group.addEditor("Code Coverage", coverageEditor);
    }

    protected void resetEditorFrom(TestNGConfiguration s) {
        editor.resetEditorFrom(s);
        coverageEditor.resetEditorFrom(s);
    }

    protected void applyEditorTo(TestNGConfiguration s) throws ConfigurationException {
        editor.applyEditorTo(s);
        coverageEditor.applyEditorTo(s);
    }

    @NotNull
    protected JComponent createEditor() {
        JTabbedPane pane = new JTabbedPane();
        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        pane.add("Configuration", editor.createEditor());
        pane.add("Code Coverage", coverageEditor.createEditor());

        return pane;
    }

    protected void disposeEditor() {
        editor.disposeEditor();
        coverageEditor.disposeEditor();
        //group.disposeEditor();
    }
}