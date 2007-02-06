/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 11/09/2006
 * Time: 19:51:32
 */
package com.theoryinpractice.testng.intention;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiRecursiveElementVisitor;

public class AnnotationsVisitor extends PsiRecursiveElementVisitor
{
    boolean hasAnnotations;

    @Override
    public void visitModifierList(PsiModifierList list) {
        if (hasAnnotations) return;
        for (PsiAnnotation annotation : list.getAnnotations()) {
            if (annotation.getQualifiedName() != null && annotation.getQualifiedName().startsWith("org.testng.annotations")) {
                hasAnnotations = true;
            }
        }
    }
}