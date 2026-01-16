package com.decisym.sysml.validator;

import org.omg.sysml.interactive.SysMLInteractive;
import org.omg.sysml.interactive.SysMLInteractiveResult;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.diagnostics.Severity;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Command-line validator for SysML v2 files.
 *
 * <p>Uses the SysML v2 Pilot Implementation to parse and validate files,
 * reporting errors in GNU format for integration with editors and build tools.
 */
@Command(
    name = "validate-sysml",
    description = "Validate SysML v2 files using the OMG Pilot Implementation.",
    mixinStandardHelpOptions = true,
    version = "validate-sysml 1.0.0"
)
public class SysMLValidator implements Callable<Integer> {

    @Parameters(
        paramLabel = "FILE",
        description = "SysML files or directories to validate. " +
                      "Directories are scanned recursively for .sysml files."
    )
    private List<File> files;

    private boolean hasErrors = false;

    /**
     * Print validation issue in GNU format for editor integration.
     * Format: filename:line:column: severity: message
     */
    private void printIssue(String filename, Issue issue) {
        String severity = issue.getSeverity().toString().toLowerCase();
        Integer line = issue.getLineNumber();
        Integer column = issue.getColumn();
        String message = issue.getMessage();

        int lineNum = (line != null) ? line : 1;
        int colNum = (column != null) ? column : 1;

        System.err.println(filename + ":" + lineNum + ":" + colNum + ": " + severity + ": " + message);

        if (issue.getSeverity() == Severity.ERROR) {
            hasErrors = true;
        }
    }

    /**
     * Validate a single SysML file and report all issues.
     */
    private boolean validateFile(SysMLInteractive interactive, Path filePath) {
        try {
            String content = Files.readString(filePath);
            String filename = filePath.getFileName().toString();

            SysMLInteractiveResult result = interactive.process(content, true);

            if (result.getException() != null) {
                System.err.println(filename + ":1:1: error: " + result.getException().getMessage());
                hasErrors = true;
                return false;
            }

            List<Issue> issues = result.getIssues();
            if (issues != null && !issues.isEmpty()) {
                for (Issue issue : issues) {
                    printIssue(filename, issue);
                }
            }

            if (result.hasErrors()) {
                hasErrors = true;
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println(filePath.getFileName() + ":1:1: error: " + e.getMessage());
            hasErrors = true;
            return false;
        }
    }

    @Override
    public Integer call() throws Exception {
        if (files == null || files.isEmpty()) {
            System.err.println("Error: No files specified. Use -h for help.");
            return 1;
        }

        List<Path> sysmlFiles = new ArrayList<>();

        for (File fileOrDir : files) {
            if (!fileOrDir.exists()) {
                System.err.println("Error: File or directory not found: " + fileOrDir);
                return 1;
            }

            if (fileOrDir.isFile()) {
                if (!fileOrDir.getName().endsWith(".sysml")) {
                    System.err.println("Error: File must have .sysml extension: " + fileOrDir);
                    return 1;
                }
                sysmlFiles.add(fileOrDir.toPath());
            } else {
                List<Path> dirFiles = Files.walk(fileOrDir.toPath())
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".sysml"))
                    .sorted()
                    .collect(Collectors.toList());
                sysmlFiles.addAll(dirFiles);
            }
        }

        if (sysmlFiles.isEmpty()) {
            System.err.println("Warning: No .sysml files found");
            return 0;
        }

        SysMLInteractive interactive = SysMLInteractive.getInstance();

        String sysmlLibraryPath = System.getProperty("sysml.library", "sysml.library");
        File libraryDir = new File(sysmlLibraryPath);
        if (!libraryDir.isDirectory()) {
            System.err.println("Error: SysML library not found: " + sysmlLibraryPath);
            System.err.println("Set via: java -Dsysml.library=/path/to/sysml.library");
            return 1;
        }
        // Use absolute path - required for EMF to handle spaces in directory names
        interactive.loadLibrary(libraryDir.getAbsolutePath());

        for (Path filePath : sysmlFiles) {
            validateFile(interactive, filePath);
        }

        if (hasErrors) {
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SysMLValidator()).execute(args);
        System.exit(exitCode);
    }
}
