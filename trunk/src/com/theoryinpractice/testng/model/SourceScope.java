package com.theoryinpractice.testng.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.execution.junit.JUnitUtil;

/**
 * @author Hani Suleiman
 *         Date: Jul 21, 2005
 *         Time: 12:22:58 PM
 */
public abstract class SourceScope
{
    private static class ModuleWithDependenciesAndLibsDependencies extends GlobalSearchScope
    {
        @Override
        public boolean contains(VirtualFile virtualfile) {
            return getSearchForFile(virtualfile) != null;
        }

        @Override
        public int compare(VirtualFile file1, VirtualFile file2) {
            GlobalSearchScope globalsearchscope = getSearchForFile(file1);
            if(globalsearchscope.contains(file2)) return globalsearchscope.compare(file1, file2);
            else return 0;
        }

        @Override
        public boolean isSearchInModuleContent(Module module) {
            return globalScope.isSearchInModuleContent(module);
        }

        @Override
        public boolean isSearchInLibraries() {
            return true;
        }

        private GlobalSearchScope getSearchForFile(VirtualFile virtualfile) {
            if(globalScope.contains(virtualfile)) return globalScope;
            for(GlobalSearchScope scope : totalScopes) {
                if(scope.contains(virtualfile)) return scope;
            }

            return null;
        }

        private final GlobalSearchScope globalScope;
        private final Collection<GlobalSearchScope> totalScopes = new ArrayList<GlobalSearchScope>();

        public ModuleWithDependenciesAndLibsDependencies(Module module) {
            globalScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            Map<Module, Collection<Module>> map = JUnitUtil.buildAllDependencies(module.getProject());
            if(map == null) return;
            for(Module dependency : map.get(module)) {
                totalScopes.add(GlobalSearchScope.moduleWithLibrariesScope(dependency));
            }
        }
    }

    private static interface ScopeForModuleEvaluator
    {
        public abstract GlobalSearchScope evaluate(Module module);
    }

    private static abstract class ModuleSourceScope extends SourceScope
    {
        @Override
        public Project getProject() {
            return project;
        }

        private final Project project;

        protected ModuleSourceScope(Project project) {
            this.project = project;
        }
    }

    public abstract GlobalSearchScope getGlobalSearchScope();

    public abstract Project getProject();

    public abstract GlobalSearchScope getLibrariesScope();

    public abstract Module[] getModulesToCompile();

    public SourceScope() {
    }

    public static SourceScope wholeProject(final Project project) {
        return new SourceScope()
        {

            @Override
            public GlobalSearchScope getGlobalSearchScope() {
                return GlobalSearchScope.allScope(project);
            }

            @Override
            public Project getProject() {
                return project;
            }

            @Override
            public Module[] getModulesToCompile() {
                return ModuleManager.getInstance(project).getModules();
            }

            @Override
            public GlobalSearchScope getLibrariesScope() {
                return getGlobalSearchScope();
            }
        };
    }

    public static SourceScope moduleWithDependencies(final Module module) {
        if(module == null) return null;
        else {
            return new ModuleSourceScope(module.getProject())
            {

                @Override
                public GlobalSearchScope getGlobalSearchScope() {
                    return GlobalSearchScope.moduleWithDependenciesScope(module);
                }

                @Override
                public GlobalSearchScope getLibrariesScope() {
                    return new ModuleWithDependenciesAndLibsDependencies(module);
                }

                @Override
                public Module[] getModulesToCompile() {
                    return (new Module[]{module});
                }
            };
        }
    }

    public static SourceScope modulesWithDependencies(final Module amodule[]) {
        if(amodule == null || amodule.length == 0) return null;
        else {
            return new ModuleSourceScope(amodule[0].getProject())
            {

                @Override
                public GlobalSearchScope getGlobalSearchScope() {
                    return SourceScope.evaluate(amodule, new ScopeForModuleEvaluator()
                    {

                        public GlobalSearchScope evaluate(Module module) {
                            return GlobalSearchScope.moduleWithDependenciesScope(module);
                        }
                    });
                }

                @Override
                public GlobalSearchScope getLibrariesScope() {
                    return SourceScope.evaluate(amodule, new ScopeForModuleEvaluator()
                    {

                        public GlobalSearchScope evaluate(Module module) {
                            return new ModuleWithDependenciesAndLibsDependencies(module);
                        }
                    });
                }

                @Override
                public Module[] getModulesToCompile() {
                    return amodule;
                }
            };
        }
    }

    public static SourceScope singleModule(final Module module) {
        if(module == null) return null;
        else {
            return new ModuleSourceScope(module.getProject())
            {

                @Override
                public GlobalSearchScope getGlobalSearchScope() {
                    return GlobalSearchScope.moduleScope(module);
                }

                @Override
                public GlobalSearchScope getLibrariesScope() {
                    return GlobalSearchScope.moduleWithLibrariesScope(module);
                }

                @Override
                public Module[] getModulesToCompile() {
                    return (new Module[]{module});
                }
            };
        }
    }

    private static GlobalSearchScope evaluate(Module amodule[], ScopeForModuleEvaluator scopeformoduleevaluator) {
        GlobalSearchScope globalsearchscope = scopeformoduleevaluator.evaluate(amodule[0]);
        for(int i = 1; i < amodule.length; i++) {
            Module module = amodule[i];
            GlobalSearchScope globalsearchscope1 = scopeformoduleevaluator.evaluate(module);
            globalsearchscope = globalsearchscope.uniteWith(globalsearchscope1);
        }

        return globalsearchscope;
    }

    public static SourceScope modules(final Module amodule[]) {
        if(amodule == null || amodule.length == 0) return null;
        else {
            return new ModuleSourceScope(amodule[0].getProject())
            {

                @Override
                public GlobalSearchScope getGlobalSearchScope() {
                    return SourceScope.evaluate(amodule, new ScopeForModuleEvaluator()
                    {

                        public GlobalSearchScope evaluate(Module module) {
                            return GlobalSearchScope.moduleScope(module);
                        }
                    });
                }

                @Override
                public GlobalSearchScope getLibrariesScope() {
                    return SourceScope.evaluate(amodule, new ScopeForModuleEvaluator()
                    {

                        public GlobalSearchScope evaluate(Module module) {
                            return GlobalSearchScope.moduleWithLibrariesScope(module);
                        }

                    });
                }

                @Override
                public Module[] getModulesToCompile() {
                    return amodule;
                }

            };
        }
    }

}
