package com.theoryinpractice.testng.ui;

import java.io.File;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diff.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.theoryinpractice.testng.Printable;

/**
 * @author Hani Suleiman Date: Dec 1, 2006 Time: 12:14:04 PM
 */
public class DiffHyperLink implements Printable
{
    private final String expected;
    private final String actual;
    private final String filePath;
    private final HyperlinkInfo hyperlink = new HyperlinkInfo()
    {
        public void navigate(Project project) {
            openDiff(project);
        }
    };
    private String title;

    public void openDiff(Project project) {
        String s = "Expected value";
        DiffContent fileContent;
        VirtualFile virtualfile;
        if (filePath != null && (virtualfile = LocalFileSystem.getInstance().findFileByPath(filePath)) != null) {
            fileContent = DiffContent.fromFile(project, virtualfile);
            s =  s + " (" + virtualfile.getPresentableUrl() + ')';
        } else {
            fileContent = new SimpleContent(expected);
        }
        SimpleDiffRequest diff = new SimpleDiffRequest(project, title);
        diff.setContents(fileContent, new SimpleContent(actual));
        diff.setContentTitles(s, "Actual value");
        diff.addHint(DiffTool.HINT_SHOW_NOT_MODAL_DIALOG);
        diff.addHint(DiffTool.HINT_DO_NOT_IGNORE_WHITESPACES);
        diff.setGroupKey('#' + com.theoryinpractice.testng.ui.TestNGResults.class.getName());
        DiffManager.getInstance().getIdeaDiffTool().show(diff);
    }

    public DiffHyperLink(String expected, String right, String filePath) {
        this.expected = expected;
        this.actual = right;
        this.filePath = filePath != null ? filePath.replace(File.separatorChar, '/') : null;
    }

    public String getLeft() {
        return expected;
    }

    public String getRight() {
        return actual;
    }

    public void print(ConsoleView printer) {
        if (isMultiLine(actual) || isMultiLine(expected)) {
            printer.print(" ", ConsoleViewContentType.ERROR_OUTPUT);
            printer.printHyperlink("<Click to see difference>", hyperlink);
            printer.print("\n", ConsoleViewContentType.ERROR_OUTPUT);
        } else {
            printer.print("\n", ConsoleViewContentType.ERROR_OUTPUT);
            printer.print("Expected: ", ConsoleViewContentType.SYSTEM_OUTPUT);
            printer.print((new StringBuilder()).append(expected).append('\n').toString(), ConsoleViewContentType.ERROR_OUTPUT);
            printer.print("Actual  : ", ConsoleViewContentType.SYSTEM_OUTPUT);
            printer.print((new StringBuilder()).append(actual).append('\n').toString(), ConsoleViewContentType.ERROR_OUTPUT);
        }
    }

    private static boolean isMultiLine(String s) {
        return s.indexOf('\n') != -1;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}