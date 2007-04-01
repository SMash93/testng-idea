package com.theoryinpractice.testng.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.util.IncorrectOperationException;
import com.theoryinpractice.testng.util.Intentions;

/**
 * @author Hani Suleiman Date: Aug 2, 2005 Time: 11:09:48 PM
 */
public class ConvertJUnitIntention extends AbstractProjectIntention
{
    public ConvertJUnitIntention(Project project) {
        super(project);
    }

    public static ConvertJUnitIntention getInstance(Project project) {
        return project.getComponent(ConvertJUnitIntention.class);
    }

    public String getComponentName() {
        return "ConvertJUnit";
    }

    public String getText() {
        return "Convert JUnit TestCase to TestNG";
    }

    public boolean isAvailable(Project project, Editor editor, PsiFile file) {
        if (!file.isWritable()) return false;
        if (!(file instanceof PsiJavaFile)) return false;
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiClass[] classes = javaFile.getClasses();
        // Check we have a class?
        if (classes.length > 0) {
            PsiClass psiClass = classes[0];
            return Intentions.inheritsJUnitTestCase(psiClass) || Intentions.containsJunitAnnotions(psiClass);
        } else {
            return false;
        }

    }

    public void invoke(Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiJavaFile psiJavaFile = (PsiJavaFile) file;
        if (file != null) {
            Intentions.convert(project, psiJavaFile.getClasses()[0]);
        }
    }

    public boolean startInWriteAction() {
        return true;
    }
}
