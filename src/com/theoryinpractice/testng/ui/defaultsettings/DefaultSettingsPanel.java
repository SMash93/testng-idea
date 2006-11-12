/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 11/11/2006
 * Time: 16:29:03
 */
package com.theoryinpractice.testng.ui.defaultsettings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.*;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.table.TableView;
import com.theoryinpractice.testng.model.TestNGParametersTableModel;

public class DefaultSettingsPanel
{
    private Project project;

    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JPanel propertiesPanel;
    private TextFieldWithBrowseButton outputDirectory;
    private TableView tableView1;
    private JButton addButton;
    private JButton removeButton;

    private TestNGParametersTableModel propertiesTableModel;
    private ArrayList<Map.Entry> propertiesList;

    public DefaultSettingsPanel(Project project) {
        this.project = project;

        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                propertiesTableModel.addParameter();
            }
        });

        removeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                for (int row : tableView1.getSelectedRows()) {
                    propertiesTableModel.removeProperty(row);
                }
            }
        });

        outputDirectory.addBrowseFolderListener(
                "TestNG",
                "Select default test output directory", project,
                new FileChooserDescriptor(false, true, false, false, false, false));

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        propertiesTableModel = new TestNGParametersTableModel();
        tableView1 = new TableView(propertiesTableModel);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setData(DefaultSettings data) {

        outputDirectory.setText(data.getOutputDirectory());

        propertiesList = new ArrayList<Map.Entry>();
        propertiesList.addAll(data.getDefaultParameters().entrySet());
        propertiesTableModel.setParameterList(propertiesList);

    }

    public void getData(DefaultSettings data) {
        data.setOutputDirectory(outputDirectory.getText());
        data.getDefaultParameters().clear();
        for (Map.Entry<String, String> entry : propertiesList) {
            data.getDefaultParameters().put(entry.getKey(), entry.getValue());
        }
    }

    public boolean isModified(DefaultSettings data) {
        return false;
    }
}