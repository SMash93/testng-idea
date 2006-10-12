package com.theoryinpractice.testng.ui.actions;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.config.*;
import com.theoryinpractice.testng.model.TestNGConsoleProperties;
import com.theoryinpractice.testng.ui.TestNGResults;

public class ScrollToTestSourceAction extends ToggleBooleanProperty.Disablable
{
    private TestNGResults results;
    
    public ScrollToTestSourceAction(TestNGConsoleProperties properties)
    {
        super("Auto Scroll to Source", "Open selected test in editor", IconLoader.getIcon("/general/autoscrollToSource.png"), properties, TestNGConsoleProperties.SCROLL_TO_SOURCE);
    }

    @Override
    protected boolean isEnabled()
    {
        AbstractProperty.AbstractPropertyContainer container = getProperties();
        return isEnabled(container, results);
    }

    private static boolean isEnabled(AbstractProperty.AbstractPropertyContainer abstractpropertycontainer, TestNGResults results)
    {
        if(!TestNGConsoleProperties.TRACK_RUNNING_TEST.value(abstractpropertycontainer))
            return true;
        else
            return results != null && !results.getRoot().isInProgress();
    }

    public static boolean isScrollEnabled(TestNGResults results)
    {
        TestNGConsoleProperties properties = results.getConsoleProperties();
        return isEnabled(properties, results) && TestNGConsoleProperties.SCROLL_TO_SOURCE.value(properties);
    }

    public void setResults(TestNGResults results)
    {
        this.results = results;
    }
}
