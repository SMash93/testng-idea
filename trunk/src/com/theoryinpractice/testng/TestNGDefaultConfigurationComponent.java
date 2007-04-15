/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 11/11/2006
 * Time: 16:15:10
 */
package com.theoryinpractice.testng;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.psi.impl.source.resolve.reference.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.theoryinpractice.testng.ui.defaultsettings.DefaultSettings;
import com.theoryinpractice.testng.ui.defaultsettings.DefaultSettingsPanel;
import com.theoryinpractice.testng.util.TestNGUtil;
import org.jdom.Element;
import org.jetbrains.annotations.*;

public class TestNGDefaultConfigurationComponent implements ProjectComponent, Configurable, JDOMExternalizable
{
    public static final String KEY_NAME = "testng.defaultConfiguration";

    private Project project;
    private DefaultSettings defaultSettings = new DefaultSettings();
    private DefaultSettingsPanel defaultSettingsPanel;

    public TestNGDefaultConfigurationComponent(Project project, ReferenceProvidersRegistry registry) {
        this.project = project;
        registry.registerReferenceProvider(new TestAnnotationFilter("dependsOnMethods"), PsiLiteralExpression.class, new PsiReferenceProviderBase() {
            @NotNull
            public PsiReference[] getReferencesByElement(PsiElement element) {
                return new MethodReference[]{new MethodReference((PsiLiteralExpression)element, false)};
            }
        });
        registry.registerReferenceProvider(new TestAnnotationFilter("dependsOnGroups"), PsiLiteralExpression.class, new PsiReferenceProviderBase() {
            @NotNull
            public PsiReference[] getReferencesByElement(PsiElement element) {
                return new GroupReference[]{new GroupReference(TestNGDefaultConfigurationComponent.this, (PsiLiteralExpression)element, false)};
            }
        });
    }

    public void projectClosed() {
    }

    public void projectOpened() {

    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return KEY_NAME;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @Nls
    public String getDisplayName() {
        return "TestNG";
    }

    public Icon getIcon() {
        return IconLoader.getIcon("/resources/testng.gif");
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return "Configure default TestNG settings";
    }

    public DefaultSettings getDefaultSettings() {
        return defaultSettings;
    }

    public JComponent createComponent() {

        defaultSettingsPanel = new DefaultSettingsPanel(project);
        defaultSettingsPanel.setData(defaultSettings);

        return defaultSettingsPanel.getMainPanel();
    }

    public boolean isModified() {
        return true;
    }

    public void apply() throws ConfigurationException {
        defaultSettingsPanel.getData(defaultSettings);
    }

    public void reset() {
        defaultSettingsPanel.setData(defaultSettings);
    }

    public void disposeUIResources() {
    }

    public void readExternal(Element element) throws InvalidDataException {
        defaultSettings.readExternal(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        defaultSettings.writeExternal(element);
    }

    private static class MethodReference extends PsiReferenceBase<PsiLiteralExpression> {

        public MethodReference(PsiLiteralExpression element, boolean soft) {
            super(element, soft);
        }

        @Nullable
        public PsiElement resolve() {
            PsiClass cls = PsiUtil.getTopLevelClass(getElement());
            PsiMethod[] methods = cls.getMethods();
            String val = getValue();
            int hackIndex = val.indexOf("IntellijIdeaRulezzz ");
            if(hackIndex > -1) {
                val = val.substring(0, hackIndex) + val.substring(hackIndex + 1, val.length());
            }
            for (PsiMethod method : methods) {
                if(TestNGUtil.hasTest(method)) {
                    if(method.getName().equals(val)) {
                        return method;
                    }
                }
            }
            return null;
        }

        public Object[] getVariants() {
            List<Object> list = new ArrayList<Object>();
            PsiClass cls = PsiUtil.getTopLevelClass(getElement());
            PsiMethod current = PsiTreeUtil.getParentOfType(getElement(), PsiMethod.class);
            PsiMethod[] methods = cls.getMethods();
            for (PsiMethod method : methods) {
                if(current != null && method.getName().equals(current.getName())) continue;
                if(TestNGUtil.hasTest(method) || TestNGUtil.hasConfig(method)) {
                    list.add(LookupValueFactory.createLookupValue(method.getName(), null));
                }
            }
            return list.toArray();
        }
    }

    private static class GroupReference extends PsiReferenceBase<PsiLiteralExpression> {

        private TestNGDefaultConfigurationComponent configurationComponent;

        public GroupReference(TestNGDefaultConfigurationComponent configurationComponent, PsiLiteralExpression element, boolean soft) {
            super(element, soft);
            this.configurationComponent = configurationComponent;
        }

        @Nullable
        public PsiElement resolve() {
            return null;
        }

        public Object[] getVariants() {
            List<Object> list = new ArrayList<Object>();

            List<String> groups = configurationComponent.getDefaultSettings().getGroups();
            for (String groupName : groups) {
                list.add(LookupValueFactory.createLookupValue(groupName, null));
            }

            if (!list.isEmpty()) {
                return list.toArray();
            } else {
                return null;
            }
        }
    }

    private static class TestAnnotationFilter implements ElementFilter {

        private String parameterName;

        public TestAnnotationFilter(String parameterName) {
            this.parameterName = parameterName;
        }

        public boolean isAcceptable(Object element, PsiElement context) {
            PsiNameValuePair pair = PsiTreeUtil.getParentOfType(context, PsiNameValuePair.class);
            if(pair == null) return false;
            if(!pair.getName().equals(parameterName)) return false;
            PsiAnnotation annotation = PsiTreeUtil.getParentOfType(pair, PsiAnnotation.class);
            if(annotation == null) return false;
            if(!TestNGUtil.isTestNGAnnotation(annotation)) return false;
            return true;
        }

        public boolean isClassAcceptable(Class hintClass) {
            return hintClass == PsiLiteralExpression.class;
        }
    }
}