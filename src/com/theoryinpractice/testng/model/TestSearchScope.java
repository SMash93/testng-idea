package com.theoryinpractice.testng.model;

import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.theoryinpractice.testng.model.SourceScope;
import com.theoryinpractice.testng.TestNGConfiguration;
import org.jdom.Element;

/**
 * @author Hani Suleiman
 *         Date: Jul 21, 2005
 *         Time: 12:19:38 PM
 */
public interface TestSearchScope
{
    public static class Wrapper implements JDOMExternalizable
    {
        private TestSearchScope testScope;

        public Wrapper() {
            testScope = TestSearchScope.WHOLE_PROJECT;
        }
        
        public void readExternal(Element element)
            throws InvalidDataException
        {
            String value = element.getAttributeValue("defaultName");
            if("singleModule".equals(value))
                testScope = TestSearchScope.SINGLE_MODULE;
            else
            if("moduleWithDependencies".equals(value))
                testScope = TestSearchScope.MODULE_WITH_DEPENDENCIES;
            else
                testScope = TestSearchScope.WHOLE_PROJECT;
        }

        public void writeExternal(Element element) throws WriteExternalException
        {
            String value = "wholeProject";
            if(testScope == TestSearchScope.SINGLE_MODULE)
                value = "singleModule";
            else
            if(testScope == TestSearchScope.MODULE_WITH_DEPENDENCIES)
                value = "moduleWithDependencies";
            element.setAttribute("defaultName", value);
        }

        public TestSearchScope getScope()
        {
            return testScope;
        }

        public void setScope(TestSearchScope testseachscope)
        {
            testScope = testseachscope;
        }
    }
    
    public abstract SourceScope getSourceScope(TestNGConfiguration config);

    public static final TestSearchScope WHOLE_PROJECT = new TestSearchScope() {

        public SourceScope getSourceScope(TestNGConfiguration junitconfiguration)
        {
            return SourceScope.wholeProject(junitconfiguration.getProject());
        }

        @Override
        public String toString()
        {
            return "WHOLE_PROJECT";
        }
    };
    
    public static final TestSearchScope SINGLE_MODULE = new TestSearchScope() {

        public SourceScope getSourceScope(TestNGConfiguration config)
        {
            return SourceScope.modules(config.getModules());
        }

        @Override
        public String toString()
        {
            return "SINGLE_MODULE";
        }
    };
    
    public static final TestSearchScope MODULE_WITH_DEPENDENCIES = new TestSearchScope() {

        public SourceScope getSourceScope(TestNGConfiguration config)
        {
            return SourceScope.modulesWithDependencies(config.getModules());
        }

        @Override
        public String toString()
        {
            return "MODULE_WITH_DEPENDENCIES";
        }
    };
}
