package com.theoryinpractice.testng.intention;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;

/**
 * @author Hani Suleiman Date: Aug 3, 2005 Time: 4:20:42 PM
 */
public abstract class AbstractProjectIntention implements ProjectComponent, IntentionAction
{
    protected Project project;

    protected AbstractProjectIntention(Project project) {
        this.project = project;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void initComponent() {
        IntentionManager.getInstance().addAction(this);
    }

    public void disposeComponent() {
    }

    public String getFamilyName() {
        return "TestNG";
    }
}
