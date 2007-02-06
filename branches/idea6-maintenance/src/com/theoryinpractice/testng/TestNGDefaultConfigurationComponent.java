/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 11/11/2006
 * Time: 16:15:10
 */
package com.theoryinpractice.testng;

import javax.swing.*;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.theoryinpractice.testng.ui.defaultsettings.DefaultSettings;
import com.theoryinpractice.testng.ui.defaultsettings.DefaultSettingsPanel;
import org.jdom.Element;
import org.jetbrains.annotations.*;

public class TestNGDefaultConfigurationComponent implements ProjectComponent, Configurable, JDOMExternalizable
{

    public static final String KEY_NAME = "testng.defaultConfiguration";

    private Project project;
    private DefaultSettings defaultSettings = new DefaultSettings();
    private DefaultSettingsPanel defaultSettingsPanel;

    public TestNGDefaultConfigurationComponent(Project project) {
        this.project = project;
    }

    public void projectClosed() {
    }

    public void projectOpened() {
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return KEY_NAME;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nls
    public String getDisplayName() {
        return "TestNG";
    }

    public Icon getIcon() {
        return IconLoader.getIcon("/resources/testng.gif");
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return "Configure default TestNG settings";
    }

    public DefaultSettings getDefaultSettings() {
        return defaultSettings;
    }

    public JComponent createComponent() {

        defaultSettingsPanel = new DefaultSettingsPanel(project);
        defaultSettingsPanel.setData(defaultSettings);

        return defaultSettingsPanel.getMainPanel();
    }

    public boolean isModified() {
        return true;
    }

    public void apply() throws ConfigurationException {
        defaultSettingsPanel.getData(defaultSettings);
    }

    public void reset() {
        defaultSettingsPanel.setData(defaultSettings);
    }

    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void readExternal(Element element) throws InvalidDataException {
        defaultSettings.readExternal(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        defaultSettings.writeExternal(element);
    }
}