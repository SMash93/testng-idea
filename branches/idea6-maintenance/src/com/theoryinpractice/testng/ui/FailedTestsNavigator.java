package com.theoryinpractice.testng.ui;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.theoryinpractice.testng.model.TestProxy;
import com.theoryinpractice.testng.model.TestFilter;

import java.util.List;

public class FailedTestsNavigator implements OccurenceNavigator
{
    private static final String NEXT_FAILED = "Next Failed Test";
    private static final String PREV_FAILED = "Previous Failed Test";
    private TestNGResults results;

    private class PreviousFailedTestInfo extends FailedTestInfo
    {
        @Override
        protected int nextIndex(int i) {
            return i - 1;
        }

        @Override
        protected int getBoundIndex() {
            return 0;
        }
    }

    private class NextFailedTestInfo extends FailedTestInfo
    {
        @Override
        protected int nextIndex(int i) {
            return i + 1;
        }

        @Override
        protected int getBoundIndex() {
            return getDefectsCount() - 1;
        }
    }

    abstract class FailedTestInfo
    {
        private TestProxy proxy;
        private List<TestProxy> allTests;
        private List<TestProxy> defects;

        FailedTestInfo() {
            super();
            proxy = null;
        }

        protected abstract int nextIndex(int i);

        protected abstract int getBoundIndex();

        private TestProxy getProxy() {
            return proxy;
        }

        private OccurenceInfo getOccurence() {
            return new OccurenceInfo(getNavigatable(), getDefectIndex(), getDefectsCount());
        }

        private int getDefectIndex() {
            return proxy != null ? defects.indexOf(proxy) : getDefectsCount();
        }

        private Navigatable getNavigatable() {
            PsiElement element = proxy.getPsiElement();
            Navigatable item = null;
            if(element instanceof PsiClass) {
                item = new OpenFileDescriptor(element.getProject(), element.getContainingFile().getVirtualFile());
            } else if(element instanceof PsiMethod) {
                item = new OpenFileDescriptor(element.getProject(), element.getContainingFile().getVirtualFile(),
                                              ((PsiMethod)element).getBody().getLBrace().getTextRange().getEndOffset());
            }
            return item;
        }

        public FailedTestInfo execute() {
            int index = getTestIndex();
            if(index == -1)
                return this;
            TestProxy proxy = getDefect(index);
            if(proxy == null)
                return this;
            if(proxy != results.getTree().getSelectedTest()) {
                this.proxy = proxy;
                return this;
            }
            int j = defects.indexOf(proxy);
            if(j == -1 || j == getBoundIndex()) {
                return this;
            } else {
                this.proxy = defects.get(nextIndex(j));
                return this;
            }
        }

        private TestProxy getDefect(int index) {
            for(int i = nextIndex(index); 0 <= i && i < allTests.size(); i = nextIndex(i)) {
                TestProxy proxy = allTests.get(i);
                if(TestFilter.DEFECTIVE_LEAF.shouldAccept(proxy))
                    return proxy;
            }

            return null;
        }

        private int getTestIndex() {
            allTests = results.getRoot().getAllTests();
            defects = TestFilter.DEFECTIVE_LEAF.select(allTests);
            return allTests.indexOf(results.getTree().getSelectedTest());
        }

        protected int getDefectsCount() {
            return defects.size();
        }

        private boolean hasProxy() {
            return proxy != null;
        }
    }

    public FailedTestsNavigator() {
    }

    public void setResults(TestNGResults results) {
        this.results = results;
    }

    public boolean hasNextOccurence() {
        return results != null && getNextInfo().hasProxy();
    }

    public boolean hasPreviousOccurence() {
        return results != null && getPrevInfo().hasProxy();
    }

    public OccurenceInfo goNextOccurence() {
        FailedTestInfo info = getNextInfo();
        results.selectTest(info.getProxy());
        return info.getOccurence();
    }

    public OccurenceInfo goPreviousOccurence() {
        FailedTestInfo info = getPrevInfo();
        results.selectTest(info.getProxy());
        return info.getOccurence();
    }

    public String getNextOccurenceActionName() {
        return NEXT_FAILED;
    }

    public String getPreviousOccurenceActionName() {
        return PREV_FAILED;
    }

    private FailedTestInfo getNextInfo() {
        return (new NextFailedTestInfo()).execute();
    }

    private FailedTestInfo getPrevInfo() {
        return (new PreviousFailedTestInfo()).execute();
    }
}
