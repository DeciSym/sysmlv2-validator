package com.decisym.sysml.validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.omg.sysml.interactive.SysMLInteractive;
import org.omg.sysml.interactive.SysMLInteractiveResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SysML v2 validation using the Pilot Implementation.
 */
class SysMLValidatorTest {

    private static SysMLInteractive interactive;
    private static boolean libraryLoaded = false;

    @BeforeAll
    static void setUp() {
        String libraryPath = System.getProperty("sysml.library",
                "target/sysml-download/sysml/sysml.library");

        File libraryDir = new File(libraryPath);
        if (!libraryDir.isDirectory()) {
            System.err.println("SKIP: SysML library not found at " + libraryPath);
            System.err.println("Run 'mvn -Psetup-dependency initialize' first");
            return;
        }

        try {
            interactive = SysMLInteractive.getInstance();
            // Use absolute path - required for EMF to handle spaces in directory names
            interactive.loadLibrary(libraryDir.getAbsolutePath());
            libraryLoaded = true;
        } catch (Exception e) {
            System.err.println("SKIP: Failed to load SysML library: " + e.getMessage());
        }
    }

    @Test
    void validFileShouldPass() throws Exception {
        if (!libraryLoaded) {
            System.err.println("SKIP: Library not loaded");
            return;
        }

        Path validFile = Path.of("src/test/resources/simple-valid.sysml");
        assertTrue(Files.exists(validFile), "Test file should exist");

        String content = Files.readString(validFile);
        SysMLInteractiveResult result = interactive.process(content, true);

        assertNull(result.getException(), "Should not throw exception");
        if (result.hasErrors()) {
            System.err.println("Validation errors in " + validFile + ":");
            for (var issue : result.getIssues()) {
                System.err.println("  " + issue.getLineNumber() + ": " + issue.getMessage());
            }
        }
        assertFalse(result.hasErrors(), "Valid file should not have errors");
    }

    @Test
    void invalidFileShouldFail() throws Exception {
        if (!libraryLoaded) {
            System.err.println("SKIP: Library not loaded");
            return;
        }

        Path invalidFile = Path.of("src/test/resources/simple-invalid.sysml");
        assertTrue(Files.exists(invalidFile), "Test file should exist");

        String content = Files.readString(invalidFile);
        SysMLInteractiveResult result = interactive.process(content, true);

        assertTrue(result.hasErrors() || result.getException() != null,
                "Invalid file should have errors or exception");
    }
}
