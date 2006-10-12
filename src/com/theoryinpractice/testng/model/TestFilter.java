package com.theoryinpractice.testng.model;

import java.util.*;

/**
 * @author Hani Suleiman Date: Aug 1, 2005 Time: 1:46:33 AM
 */
public abstract class TestFilter
{
    private static class NotFilter extends TestFilter
    {
        private final TestFilter filter;

        @Override
        public boolean shouldAccept(TestProxy proxy) {
            return !filter.shouldAccept(proxy);
        }

        public NotFilter(TestFilter filter) {
            this.filter = filter;
        }
    }

    private static class AndFilter extends TestFilter
    {
        private final TestFilter filter1;
        private final TestFilter filter2;

        @Override
        public boolean shouldAccept(TestProxy proxy) {
            return filter1.shouldAccept(proxy) && filter2.shouldAccept(proxy);
        }

        public AndFilter(TestFilter filter1, TestFilter filter2) {
            this.filter1 = filter1;
            this.filter2 = filter2;
        }
    }

    public abstract boolean shouldAccept(TestProxy proxy);

    private TestFilter() {
    }

    public List<TestProxy> select(List<TestProxy> list) {
        ArrayList<TestProxy> results = new ArrayList<TestProxy>();
        for(TestProxy proxy : list) {
            if(shouldAccept(proxy)) {
                results.add(proxy);
            }
        }
        return results;
    }

    public TestProxy detectIn(Collection<TestProxy> collection) {
        for(TestProxy proxy : collection) {
            if(shouldAccept(proxy))
                return proxy;
        }

        return null;
    }

    private TestFilter not() {
        return new NotFilter(this);
    }

    private TestFilter and(TestFilter filter) {
        return new AndFilter(this, filter);
    }

    public static final TestFilter IN_PROGRESS = new TestFilter()
    {

        @Override
        public boolean shouldAccept(TestProxy proxy) {
            return proxy.isInProgress();
        }
    };

    public static final TestFilter NO_FILTER = new TestFilter()
    {

        @Override
        public boolean shouldAccept(TestProxy proxy) {
            return true;
        }

    };

    public static final TestFilter NOT_PASSED = new TestFilter()
    {

        @Override
        public boolean shouldAccept(TestProxy proxy) {
            return proxy.isNotPassed();
        }
    };

    public static final TestFilter DEFECT;
    public static final TestFilter LEAF;
    public static final TestFilter NOT_LEAF;
    public static final TestFilter DEFECTIVE_LEAF;

    static {
        DEFECT = new TestFilter()
        {

            @Override
            public boolean shouldAccept(TestProxy proxy) {
                return proxy.isNotPassed();
            }

        };
        LEAF = new TestFilter()
        {

            @Override
            public boolean shouldAccept(TestProxy proxy) {
                return proxy.isResult();
            }
        };

        NOT_LEAF = LEAF.not();
        DEFECTIVE_LEAF = DEFECT.and(LEAF);
    }
}  