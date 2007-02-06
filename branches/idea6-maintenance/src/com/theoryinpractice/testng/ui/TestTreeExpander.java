package com.theoryinpractice.testng.ui;

import javax.swing.*;

import com.intellij.ide.TreeExpander;
import com.intellij.util.ui.tree.TreeUtil;

class TestTreeExpander implements TreeExpander
{
    private TestNGResults results;

    TestTreeExpander() {
    }

    public void setResults(TestNGResults results) {
        this.results = results;
    }

    public void expandAll() {
        JTree tree = results.getTree();
        for(int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);
    }

    public boolean canExpand() {
        return results.getRoot().getResults().size() > 0;
    }

    public void collapseAll() {
        TreeUtil.collapseAll(results.getTree(), 1);
    }

    public boolean canCollapse() {
        return results.getRoot().getResults().size() > 0;
    }
}
