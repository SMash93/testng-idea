package com.theoryinpractice.testng.ui;

import com.intellij.execution.junit2.ui.TestsUIUtil;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.actions.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.config.ToggleBooleanProperty;
import com.theoryinpractice.testng.model.TestNGConsoleProperties;
import com.theoryinpractice.testng.ui.actions.TestNGActions;
import com.theoryinpractice.testng.ui.actions.ScrollToTestSourceAction;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ToolbarPanel extends JPanel implements OccurenceNavigator
{
    private TestTreeExpander expander = new TestTreeExpander();
    private FailedTestsNavigator failedTestsNavigator = new FailedTestsNavigator();
    private ScrollToTestSourceAction scrollToSource;
    
    public ToolbarPanel()
    {}
    
    public ToolbarPanel(TestNGConsoleProperties properties, TestNGResults results)
    {
        super(new BorderLayout());
        add(new JLabel(IconLoader.getIcon("/general/inactiveSeparator.png")), BorderLayout.WEST);
        DefaultActionGroup group = new DefaultActionGroup(null, false);
        group.addSeparator();
        group.add(new ToggleBooleanProperty("Hide Passed", "Hide passed tests", TestsUIUtil.loadIcon("hidePassed"), properties, TestNGConsoleProperties.HIDE_PASSED_TESTS));
        group.add(new ToggleBooleanProperty("Track Running Test", "Select currently running test in tree", TestsUIUtil.loadIcon("trackTests"), properties, TestNGConsoleProperties.TRACK_RUNNING_TEST));
        group.addSeparator();
        group.add(new CollapseAllToolbarAction(expander, "Collapse all test suites"));
        group.add(new ExpandAllToolbarAction(expander, "Expand all test suites"));
        group.addSeparator();
        group.add(new PreviousOccurenceToolbarAction(failedTestsNavigator));
        group.add(new NextOccurenceToolbarAction(failedTestsNavigator));
        group.addSeparator();
        group.add(new ToggleBooleanProperty("Select First Failed Test When Finished", null, TestsUIUtil.loadIcon("selectFirstDefect"), properties, TestNGConsoleProperties.SELECT_FIRST_DEFECT));
        //group.add(new ToggleBooleanProperty("Scroll to Stacktrace", "Scroll console to beginning of assertion or exception stacktrace", IconLoader.getIcon("/runConfigurations/scrollToStackTrace.png"), properties, TestNGConsoleProperties.SCROLL_TO_STACK_TRACE));
        scrollToSource = new ScrollToTestSourceAction(properties);
        group.add(scrollToSource);
        //group.add(new ToggleBooleanProperty("Open Source at Exception", "Go to line which caused exception when opening test source", IconLoader.getIcon("/runConfigurations/sourceAtException.png"), properties, TestNGConsoleProperties.OPEN_FAILURE_LINE));
        setResults(results);
        add(ActionManager.getInstance().createActionToolbar("TestTreeViewToolbar", group, true).getComponent(), BorderLayout.CENTER);
    }

    public void setResults(final TestNGResults results)
    {
        TestNGActions.installFilterAction(results);
        scrollToSource.setResults(results);
        expander.setResults(results);
        failedTestsNavigator.setResults(results);
    }

    public boolean hasNextOccurence()
    {
        return failedTestsNavigator.hasNextOccurence();
    }

    public boolean hasPreviousOccurence()
    {
        return failedTestsNavigator.hasPreviousOccurence();
    }

    public OccurenceInfo goNextOccurence()
    {
        return failedTestsNavigator.goNextOccurence();
    }

    public OccurenceInfo goPreviousOccurence()
    {
        return failedTestsNavigator.goPreviousOccurence();
    }

    public String getNextOccurenceActionName()
    {
        return failedTestsNavigator.getNextOccurenceActionName();
    }

    public String getPreviousOccurenceActionName()
    {
        return failedTestsNavigator.getPreviousOccurenceActionName();
    }
}
