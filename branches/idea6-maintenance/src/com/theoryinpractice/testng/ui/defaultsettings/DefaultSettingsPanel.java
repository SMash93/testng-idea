/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 11/11/2006
 * Time: 16:29:03
 */
package com.theoryinpractice.testng.ui.defaultsettings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.table.TableView;
import com.theoryinpractice.testng.model.TestNGParametersTableModel;
import com.theoryinpractice.testng.model.TestNGGroupsTableModel;

public class DefaultSettingsPanel
{
    private Project project;

    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JPanel propertiesPanel;
    private TextFieldWithBrowseButton outputDirectory;
    private TableView tableView1;
    private JButton addParameterButton;
    private JButton removeParameterButton;
    private JTabbedPane tabbedPane2;
    private JButton addGroupButton;
    private JButton removeGroupButton;
    private JTable groupTable;

    private TestNGParametersTableModel propertiesTableModel;
    private TestNGGroupsTableModel groupsTableModel;
    private ArrayList<Map.Entry> propertiesList;

    public DefaultSettingsPanel(final Project project) {
        this.project = project;

        addParameterButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                propertiesTableModel.addParameter();
            }
        });

        removeParameterButton.addActionListener(new ActionListener()
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

        removeGroupButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                for (int row : groupTable.getSelectedRows()) {
                    groupsTableModel.removeProperty(row);
                }
            }
        });
        addGroupButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                groupsTableModel.addParameter();                
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        propertiesTableModel = new TestNGParametersTableModel();
        groupsTableModel = new TestNGGroupsTableModel();
        tableView1 = new TableView(propertiesTableModel);
        groupTable = new TableView(groupsTableModel);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setData(DefaultSettings data) {

        outputDirectory.setText(data.getOutputDirectory());

        propertiesList = new ArrayList<Map.Entry>();
        propertiesList.addAll(data.getDefaultParameters().entrySet());
        propertiesTableModel.setParameterList(propertiesList);
        groupsTableModel.setGroupList(data.getGroups());

    }

    public void getData(DefaultSettings data) {
        data.setOutputDirectory(outputDirectory.getText());
        data.getDefaultParameters().clear();
        for (Map.Entry<String, String> entry : propertiesList) {
            data.getDefaultParameters().put(entry.getKey(), entry.getValue());
        }

        data.setGroups(groupsTableModel.getItems());
    }

    public boolean isModified(DefaultSettings data) {
        return false;
    }
}