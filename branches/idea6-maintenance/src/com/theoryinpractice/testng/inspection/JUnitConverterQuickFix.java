package com.theoryinpractice.testng.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.util.IncorrectOperationException;
import com.theoryinpractice.testng.util.Intentions;

/**
 * @author Hani Suleiman Date: Aug 3, 2005 Time: 3:41:50 AM
 */
public class JUnitConverterQuickFix implements LocalQuickFix
{
    private static final Logger LOGGER = Logger.getInstance("TestNG QuickFix");
    
    public String getName() {
        return "Convert TestCase to TestNG";
    }

    public void applyFix(Project project, ProblemDescriptor descriptor) {
        PsiClass psiClass = (PsiClass)descriptor.getPsiElement();
        try {
            Intentions.convert(project, psiClass);
        } catch(IncorrectOperationException e) {
            LOGGER.error("Error converting testcase", e);
        }
    }

    //to appear in "Apply Fix" statement when multiple Quick Fixes exist
    public String getFamilyName() {
        return "TestNG";
    }
}
