package com.theoryinpractice.testng.util;

import java.util.Arrays;
import java.util.List;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author Hani Suleiman Date: Aug 3, 2005 Time: 3:32:31 AM
 */
public class Intentions
{
    private static Logger LOG = Logger.getInstance("TestNG");

    public static void convert(Project project, PsiClass psiClass) throws IncorrectOperationException {
        PsiManager manager = PsiManager.getInstance(project);
        PsiElementFactory factory = manager.getElementFactory();
        PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();

        if (javaFile.getLanguageLevel().hasEnumKeywordAndAutoboxing()) {
            javaFile.getImportList().add(factory.createImportStatementOnDemand("org.testng.annotations"));
        }

        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        for (PsiMethod method : psiClass.getMethods()) {
            if (method.isConstructor()) {
                convertJUnitConstructor(method);
            } else {
                if (!javaFile.getLanguageLevel().hasEnumKeywordAndAutoboxing()) {
                    addMethodJavadoc(factory, method);
                } else {
                    if (containsJunitAnnotions(method)) {
                        convertJunitAnnotions(factory, method);
                    } else {
                        addMethodAnnotations(factory, method);
                    }
                }
            }

            PsiMethodCallExpression[] methodCalls = getTestCaseCalls(method);
            for (PsiMethodCallExpression methodCall : methodCalls) {
                PsiMethod assertMethod = methodCall.resolveMethod();
                if (assertMethod == null) {
                    continue;
                }
                PsiAssertStatement assertStatement = null;
                String methodName = assertMethod.getName();
                PsiExpression[] expressions = methodCall.getArgumentList().getExpressions();
                if ("assertTrue".equals(methodName) || "assertFalse".equals(methodName)) {
                    if (expressions.length == 1) {
                        assertStatement = createAssert(factory, null, methodCall);
                        assertStatement.getAssertCondition().replace(expressions[0]);
                    } else if (expressions.length == 2) {
                        assertStatement = createAssert(factory, expressions[0], methodCall);
                        assertStatement.getAssertCondition().replace(expressions[1]);
                    }

                    if ("assertFalse".equals(methodName) && assertStatement != null) {
                        addNegation(factory, assertStatement.getAssertCondition());
                    }
                } else if ("assertNull".equals(methodName) || "assertNotNull".equals(methodName)) {
                    String operator = "assertNull".equals(methodName) ? "==" : "!=";
                    if (expressions.length == 1) {
                        assertStatement = createAssert(factory, null, methodCall);
                        PsiExpression expression = factory.createExpressionFromText(expressions[0].getText() + ' ' + operator + " null", assertStatement);
                        assertStatement.getAssertCondition().replace(expression);
                    } else if (expressions.length == 2) {
                        assertStatement = createAssert(factory, expressions[0], methodCall.getParent());
                        PsiExpression expression = factory.createExpressionFromText(expressions[1].getText() + ' ' + operator + " null", assertStatement);
                        assertStatement.getAssertCondition().replace(expression);
                    }
                } else if ("fail".equals(methodName)) {
                    if (expressions.length == 0) {
                        assertStatement = createAssert(factory, null, methodCall);
                    } else if (expressions.length == 1) {
                        assertStatement = createAssert(factory, expressions[0], methodCall);
                    }
                } else {
                    //if it's a 3 arg, the error message goes at the end
                    if (expressions.length == 2) {
                        methodCall.replace(factory.createStatementFromText("Assert." + methodCall.getText(), methodCall.getParent()));
                    } else if (expressions.length == 3) {
                        String call = "Assert." + methodName + '(' + expressions[2].getText() + ", " + expressions[1].getText() + ", " + expressions[0].getText() + ')';
                        methodCall.replace(factory.createStatementFromText(call, methodCall.getParent()));
                    }
                }

                if (assertStatement != null) {
                    codeStyleManager.reformat(PsiTreeUtil.getParentOfType(methodCall, PsiStatement.class).replace(assertStatement));
                }
            }
        }
        if ("junit.framework.TestCase".equals(psiClass.getSuperClass().getQualifiedName())) {
            for (PsiJavaCodeReferenceElement element : psiClass.getExtendsList().getReferenceElements()) {
                element.delete();
            }

            PsiImportStatement testCaseImport = javaFile.getImportList().findSingleClassImportStatement("junit.framework.TestCase");
            if (testCaseImport != null)
                testCaseImport.delete();
        }
        javaFile.getImportList().add(factory.createImportStatement(manager.findClass("org.testng.Assert", GlobalSearchScope.allScope(project))));
        PsiImportStatement assertImport = javaFile.getImportList().findSingleClassImportStatement("junit.framework.Assert");
        if (assertImport != null)
            assertImport.delete();
        codeStyleManager.reformat(javaFile.getImportList());
    }

    private static void convertJunitAnnotions(PsiElementFactory factory, PsiMethod method) throws IncorrectOperationException {
        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if ("org.junit.Test".equals(annotation.getQualifiedName())) {
                PsiAnnotation newAnnotation = factory.createAnnotationFromText("@org.testng.annotations.Test", method);
                annotation.replace(newAnnotation);
            } else if ("org.junit.BeforeClass".equals(annotation.getQualifiedName())) {
                PsiAnnotation newAnnotation = factory.createAnnotationFromText("@org.testng.annotations.BeforeClass", method);
                annotation.replace(newAnnotation);
            } else if ("org.junit.Before".equals(annotation.getQualifiedName())) {
                PsiAnnotation newAnnotation = factory.createAnnotationFromText("@org.testng.annotations.BeforeMethod", method);
                annotation.replace(newAnnotation);
            } else if ("org.junit.AfterClass".equals(annotation.getQualifiedName())) {
                PsiAnnotation newAnnotation = factory.createAnnotationFromText("@org.testng.annotations.AfterClass", method);
                annotation.replace(newAnnotation);
            } else if ("org.junit.After".equals(annotation.getQualifiedName())) {
                PsiAnnotation newAnnotation = factory.createAnnotationFromText("@org.testng.annotations.AfterMethod", method);
                annotation.replace(newAnnotation);
            }
        }

    }

    public static boolean containsJunitAnnotions(PsiClass psiClass) {
        if (psiClass != null) {
            for (PsiMethod method : psiClass.getMethods()) {
                if (containsJunitAnnotions(method)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsJunitAnnotions(PsiMethod method) {
        if (method != null) {
            PsiAnnotation[] annotations = method.getModifierList().getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                if (junitAnnotions.contains(annotation.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void convertJUnitConstructor(PsiMethod method) {
        method.accept(new PsiRecursiveElementVisitor()
        {

            @Override
            public void visitExpressionStatement(PsiExpressionStatement statement) {
                PsiExpression expression = statement.getExpression();
                if (expression instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expression;
                    if (methodCall.getArgumentList().getExpressions().length == 1) {
                        PsiMethod resolved = methodCall.resolveMethod();
                        if (resolved != null
                                && "junit.framework.TestCase".equals(resolved.getContainingClass().getQualifiedName())
                                && "TestCase".equals(resolved.getName())
                                ) {
                            try {
                                statement.delete();
                            } catch (IncorrectOperationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    private static PsiMethodCallExpression[] getTestCaseCalls(PsiMethod method) {
        PsiElement[] methodCalls = PsiTreeUtil.collectElements(method, new PsiElementFilter()
        {
            public boolean isAccepted(PsiElement element) {
                if (!(element instanceof PsiMethodCallExpression)) return false;
                PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
                if (methodCall.resolveMethod() != null) {
                    PsiMethod method = methodCall.resolveMethod();
                    if ("junit.framework.Assert".equals(method.getContainingClass().getQualifiedName())) {
                        return true;
                    }
                }
                return false;
            }
        });
        PsiMethodCallExpression[] expressions = new PsiMethodCallExpression[methodCalls.length];
        System.arraycopy(methodCalls, 0, expressions, 0, methodCalls.length);
        return expressions;
    }

    private static void addMethodJavadoc(PsiElementFactory factory, PsiMethod method) throws IncorrectOperationException {
        if (method.getName().startsWith("test")) {
            addMethodJavadocLine(factory, method, " * @testng.test");
        } else if ("setUp".equals(method.getName()) && method.getParameterList().getParameters().length == 0) {
            addMethodJavadocLine(factory, method, " * @testng.before-test");
        } else if ("tearDown".equals(method.getName()) && method.getParameterList().getParameters().length == 0) {
            addMethodJavadocLine(factory, method, " * @testng.after-test");
        }
        CodeStyleManager.getInstance(method.getProject()).reformat(method.getModifierList());
    }

    private static void addMethodJavadocLine(PsiElementFactory factory, PsiMethod method, String javaDocLine) throws IncorrectOperationException {
        PsiComment newComment;
        PsiElement comment = method.getFirstChild();
        if (comment != null && comment instanceof PsiComment) {
            String[] commentLines = comment.getText().split("\n");
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < commentLines.length; i++) {
                String commentLine = commentLines[i];
                // last line, append our new comment entry
                if (i == commentLines.length - 1) {
                    buf.append(javaDocLine);
                    buf.append(commentLine);
                } else {
                    buf.append(commentLine);
                    buf.append('\n');
                }
            }
            String commentString = buf.toString();

            newComment = factory.createCommentFromText(commentString, null);
            comment.replace(newComment);

        } else {
            String commentString;

            StringBuffer commentBuffer = new StringBuffer();
            commentBuffer.append("/**\n");
            commentBuffer.append(javaDocLine);
            commentBuffer.append("\n");
            commentBuffer.append(" */");

            commentString = commentBuffer.toString();
            newComment = factory.createCommentFromText(commentString, null);

            method.addBefore(newComment, comment);
        }
    }

    private static void addMethodAnnotations(PsiElementFactory factory, PsiMethod method) throws IncorrectOperationException {
        if (method.getName().startsWith("test")) {
            PsiAnnotation annotation = factory.createAnnotationFromText("@org.testng.annotations.Test", method);
            method.getModifierList().addBefore(annotation, method.getModifierList().getFirstChild());
        } else if ("setUp".equals(method.getName()) && method.getParameterList().getParameters().length == 0) {
            PsiAnnotation annotation = factory.createAnnotationFromText("@org.testng.annotations.BeforeMethod", method);
            method.getModifierList().addBefore(annotation, method.getModifierList().getFirstChild());
        } else if ("tearDown".equals(method.getName()) && method.getParameterList().getParameters().length == 0) {
            PsiAnnotation annotation = factory.createAnnotationFromText("@org.testng.annotations.AfterMethod", method);
            method.getModifierList().addBefore(annotation, method.getModifierList().getFirstChild());
        }
        CodeStyleManager.getInstance(method.getProject()).reformat(method.getModifierList());
    }

    public static void convertAnnotationToJavadoc(PsiModifierListOwner element) throws IncorrectOperationException {
        PsiAnnotation[] annotations = element.getModifierList().getAnnotations();
        PsiElementFactory factory = element.getManager().getElementFactory();
        for (PsiAnnotation annotation : annotations) {
            if (element instanceof PsiDocCommentOwner) {
                StringBuffer text = new StringBuffer(convertAnnotationClassToJavadocElement(annotation.getQualifiedName()));

                PsiAnnotationParameterList list = annotation.getParameterList();
                for (PsiNameValuePair pair : list.getAttributes()) {
                    text.append(' ');
                    if (pair.getName() != null) {
                        text.append(pair.getName());
                    } else {
                        text.append("value");
                    }
                    text.append(" = \"");

                    String parameterText = pair.getValue().getText();
                    if (parameterText.startsWith("{")) {
                        parameterText = parameterText.replaceAll("(\\{\\\"|\\\"\\}|\\\"\\w*\\,\\w*\\\")", " ").trim();
                    }
                    text.append(parameterText);
                    text.append('\"');
                }
                PsiDocTag doc = factory.createDocTagFromText('@' + text.toString(), element);
                if (((PsiDocCommentOwner) element).getDocComment() == null) {
                    PsiDocComment docComment = factory.createDocCommentFromText("/**\n */", element);
                    element.addBefore(docComment, annotation.getParent());
                }
                PsiDocComment docComment = ((PsiDocCommentOwner) element).getDocComment();
                docComment.addAfter(doc, docComment.getFirstChild());
                annotation.delete();
            }
        }
        if (element instanceof PsiDocCommentOwner)
            CodeStyleManager.getInstance(element.getProject()).reformat(((PsiDocCommentOwner) element).getDocComment());
        CodeStyleManager.getInstance(element.getProject()).reformat(element.getModifierList());
    }

    private static String convertAnnotationClassToJavadocElement(String annotationFqn) {
        char[] chars = annotationFqn.replace("org.testng.annotations", "testng").toCharArray();

        boolean skippedFirst = false;
        StringBuffer sb = new StringBuffer();
        for (char aChar : chars) {
            if (aChar > 'A' && aChar < 'Z') {
                if (skippedFirst) {
                    sb.append("-");
                } else {
                    skippedFirst = true;
                }
            }
            sb.append(String.valueOf(aChar));
        }

        return sb.toString().toLowerCase();
    }

    private static boolean annotationHasAttribute(PsiAnnotation annotation, String attribute) {
        PsiAnnotationParameterList list = annotation.getParameterList();
        for (PsiNameValuePair pair : list.getAttributes()) {
            if (attribute.equals(pair.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void convertOldAnnotationAttributeToAnnotation(PsiModifierListOwner element, PsiAnnotation annotation, String attribute, String newAnnotation) throws IncorrectOperationException {
        LOG.info("Looking for attribute " + attribute);
        if (annotationHasAttribute(annotation, attribute)) {
            StringBuffer newAnnotationBuffer = new StringBuffer();
            newAnnotationBuffer.append(newAnnotation);

            PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
            if (attributes.length > 0) {
                newAnnotationBuffer.append("(");
                for (int i = 0; i < attributes.length; i++) {
                    PsiNameValuePair attributePair = attributes[i];
                    if (!attributePair.getName().equals(attribute)) {
                        if (i > 0) {
                            newAnnotationBuffer.append(",");
                        }
                        newAnnotationBuffer.append(attributePair.getText());
                    }
                }

                newAnnotationBuffer.append(")");
            }

            LOG.info("Replacing with new annotation: " + newAnnotationBuffer.toString());
            PsiElementFactory factory = element.getManager().getElementFactory();
            PsiAnnotation newPsiAnnotation = factory.createAnnotationFromText(newAnnotationBuffer.toString(), element);
            annotation.replace(newPsiAnnotation);
        }
    }

    public static void convertOldAnnotationToAnnotation(PsiModifierListOwner element) throws IncorrectOperationException {
        LOG.info("convertOldAnnotationToAnnotation() on " + element.getText());

        PsiAnnotation[] annotations = element.getModifierList().getAnnotations();

        LOG.info("Found " + annotations.length + " annotations - checking for legacy TestNG");
        for (PsiAnnotation annotation : annotations) {
            LOG.info("Found " + annotation.getQualifiedName());
            if (annotation.getQualifiedName().equals("org.testng.annotations.Configuration")) {
                convertOldAnnotationAttributeToAnnotation(element, annotation, "beforeTestClass", "@org.testng.annotations.BeforeTest");
                convertOldAnnotationAttributeToAnnotation(element, annotation, "beforeTestMethod", "@org.testng.annotations.BeforeMethod");
                convertOldAnnotationAttributeToAnnotation(element, annotation, "beforeSuite", "@org.testng.annotations.BeforeSuite");
                convertOldAnnotationAttributeToAnnotation(element, annotation, "beforeGroups", "@org.testng.annotations.BeforeGroups");

                convertOldAnnotationAttributeToAnnotation(element, annotation, "afterTestClass", "@org.testng.annotations.AfterTest");
                convertOldAnnotationAttributeToAnnotation(element, annotation, "afterTestMethod", "@org.testng.annotations.AfterMethod");
                convertOldAnnotationAttributeToAnnotation(element, annotation, "afterSuite", "@org.testng.annotations.AfterSuite");
                convertOldAnnotationAttributeToAnnotation(element, annotation, "afterGroups", "@org.testng.annotations.AfterGroups");
            }
        }

        if (element instanceof PsiDocCommentOwner) {
            PsiDocComment psiDocComment = ((PsiDocCommentOwner) element).getDocComment();
            if (psiDocComment != null) {
                CodeStyleManager.getInstance(element.getProject()).reformat(psiDocComment);
            }
        }
        CodeStyleManager.getInstance(element.getProject()).reformat(element.getModifierList());
    }

    public static void convertJavadocToAnnotation(PsiMethod method) throws IncorrectOperationException {
        if (method.getDocComment() == null) return;
        PsiElementFactory factory = method.getManager().getElementFactory();
        for (PsiDocTag tag : method.getDocComment().getTags()) {
            if (tag.getName().startsWith("testng.")) {
                String annotationName = tag.getText().substring("testng.".length() + 1);
                int space = annotationName.indexOf(' ');
                if (space > -1) {
                    annotationName = annotationName.substring(0, space);
                }
                char[] chars = annotationName.toCharArray();
                chars[0] = Character.toUpperCase(chars[0]);
                annotationName = new String(chars);
                StringBuffer annotationText = new StringBuffer("@");
                annotationText.append(annotationName);
                PsiClass annotationClass = method.getManager().findClass("org.testng.annotations." + annotationName, GlobalSearchScope.allScope(method.getProject()));
                PsiElement[] dataElements = tag.getDataElements();
                if (dataElements.length > 1) {
                    annotationText.append('(');
                }
                if (annotationClass != null) {
                    for (PsiMethod attribute : annotationClass.getMethods()) {
                        boolean stripQuotes = false;
                        PsiType returnType = attribute.getReturnType();
                        if (returnType.equals(PsiType.BOOLEAN)
                                || returnType.equals(PsiType.BYTE)
                                || returnType.equals(PsiType.DOUBLE)
                                || returnType.equals(PsiType.FLOAT)
                                || returnType.equals(PsiType.INT)
                                || returnType.equals(PsiType.LONG)
                                || returnType.equals(PsiType.SHORT)
                                ) {
                            stripQuotes = true;
                        }
                        for (int i = 0; i < dataElements.length; i++) {
                            String text = dataElements[i].getText();
                            int equals = text.indexOf('=');
                            String value;
                            String key;
                            if (equals == -1) {
                                key = text;
                                //no equals, so we look in the next token
                                String next = dataElements[++i].getText().trim();
                                //it's an equals by itself
                                if (next.length() == 1) {
                                    value = dataElements[++i].getText().trim();
                                } else {
                                    //otherwise, it's foo =bar, so we strip equals
                                    value = next.substring(1, next.length()).trim();
                                }
                            } else {
                                //equals in the first bit
                                key = text.substring(0, equals).trim();
                                //check if the value is in the first bit too
                                if (equals < text.length() - 1) {
                                    //we have stuff after equals, great
                                    value = text.substring(equals + 1, text.length()).trim();
                                } else {
                                    //nothing after equals, so we just get the next element
                                    value = dataElements[++i].getText().trim();
                                }
                            }
                            if (!key.equals(attribute.getName())) continue;
                            annotationText.append(key);
                            if (stripQuotes && value.charAt(0) == '\"') {
                                value = value.substring(1, value.length() - 1);
                            }
                            annotationText.append(" = ");
                            annotationText.append(value);
                        }
                    }
                }

                if (dataElements.length > 1) {
                    annotationText.append(')');
                }
                PsiAnnotation annotation = factory.createAnnotationFromText(annotationText.toString(), method);
                method.getModifierList().addBefore(annotation, method.getModifierList().getFirstChild());
                tag.delete();
                for (PsiElement element : method.getDocComment().getChildren()) {
                    //if it's anything other than a doc token, then it must stay
                    if (element instanceof PsiWhiteSpace) continue;
                    if (!(element instanceof PsiDocToken)) return;
                    PsiDocToken docToken = (PsiDocToken) element;
                    if (docToken.getTokenType() == PsiDocToken.DOC_COMMENT_DATA && docToken.getText().trim().length() > 0) {
                        return;
                    }
                }
                //at this point, our doc don't have non-empty comments, nor any tags, so we can delete it.
                method.getDocComment().delete();
            }
        }
    }

    private static PsiAssertStatement createAssert(PsiElementFactory factory, PsiExpression description, PsiElement context) throws IncorrectOperationException {
        PsiAssertStatement assertStatement;
        if (description == null) {
            assertStatement = (PsiAssertStatement) factory.createStatementFromText("assert false;", context.getParent());
            return assertStatement;
        } else {
            assertStatement = (PsiAssertStatement) factory.createStatementFromText("assert false : \"x\";", context.getParent());
            assertStatement.getAssertDescription().replace(description);
        }
        return assertStatement;
    }

    private static void addNegation(PsiElementFactory factory, PsiExpression expression) throws IncorrectOperationException {
        expression.replace(factory.createExpressionFromText("!("
                + expression.getText() + ')', PsiTreeUtil.getParentOfType(expression, PsiMethodCallExpression.class)));
    }

    public static boolean inheritsJUnitTestCase(PsiClass psiClass) {
        PsiClass current = psiClass;
        //handle typo where class extends itself
        while (current != null && current != psiClass) {
            PsiClass[] supers = current.getSupers();
            if (supers.length > 0) {
                PsiClass parent = supers[0];
                if ("junit.framework.TestCase".equals(parent.getQualifiedName()))
                    return true;
                current = parent;
            } else {
                current = null;
            }
        }
        return false;
    }

    private static final List junitAnnotions = Arrays.asList(new String[] {
            "org.junit.Test",
            "org.junit.Before",
            "org.junit.BeforeClass",
            "org.junit.After",
            "org.junit.AfterClass"
    });

}
