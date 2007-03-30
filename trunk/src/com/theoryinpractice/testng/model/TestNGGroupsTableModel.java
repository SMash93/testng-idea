/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Jul 26, 2005
 * Time: 7:33:45 PM
 */
package com.theoryinpractice.testng.model;

import java.util.*;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

public class TestNGGroupsTableModel extends ListTableModel<String>
{

    private List<String> groupList;

    public TestNGGroupsTableModel() {
        super(new ColumnInfo[] {
                new ColumnInfo.StringColumn("Group Name")

        });
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
        setItems(groupList);
    }

    public void addParameter() {
        List<String> newList = new ArrayList<String>();
        newList.add("");
        newList.addAll(groupList);
        setGroupList(newList);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String entry = groupList.get(rowIndex);
        groupList.remove(rowIndex);

        switch (columnIndex) {
            case 0:
                entry = (String) aValue;
                break;
        }


        groupList.add(rowIndex, entry);
        setGroupList(groupList);
    }

    public void removeProperty(int rowIndex) {
        groupList.remove(rowIndex);
        setGroupList(groupList);
    }
}