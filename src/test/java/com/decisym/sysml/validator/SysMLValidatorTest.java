package com.decisym.sysml.validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.omg.sysml.interactive.SysMLInteractive;
import org.omg.sysml.interactive.SysMLInteractiveResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SysML v2 validation using the Pilot Implementation.
 */
class SysMLValidatorTest {

    private static SysMLInteractive interactive;

    @BeforeAll
    static void setUp() throws Exception {
        String libraryPath = System.getProperty("sysml.library",
                "target/sysml-download/sysml/sysml.library");

        File libraryDir = new File(libraryPath);
        assertTrue(
                libraryDir.isDirectory(),
                "SysML library not found at " + libraryPath +
                        ". Run 'mvn -Psetup-dependency initialize' to bootstrap the SysML library."
        );

        interactive = SysMLInteractive.getInstance();
        interactive.loadLibrary(libraryDir.getAbsolutePath());
    }

    private static String formatIssues(SysMLInteractiveResult result) {
        if (result.getIssues() == null || result.getIssues().isEmpty()) {
            return "(no issues reported)";
        }

        return result.getIssues().stream()
                .map(issue -> issue.getLineNumber() + ":" + issue.getColumn() + ": " + issue.getMessage())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    void validFileShouldPass() throws Exception {
        Path validFile = Path.of("src/test/resources/simple-valid.sysml");
        assertTrue(Files.exists(validFile), "Test file should exist");

        String content = Files.readString(validFile);
        SysMLInteractiveResult result = interactive.process(content, true);

        assertNull(result.getException(), "Should not throw exception");
        assertFalse(result.hasErrors(),
                () -> "Valid file should not have errors:" + System.lineSeparator() + formatIssues(result));
    }

    @Test
    void invalidFileShouldFail() throws Exception {
        Path invalidFile = Path.of("src/test/resources/simple-invalid.sysml");
        assertTrue(Files.exists(invalidFile), "Test file should exist");

        String content = Files.readString(invalidFile);
        SysMLInteractiveResult result = interactive.process(content, true);

        assertTrue(result.hasErrors() || result.getException() != null,
                "Invalid file should have errors or exception");
    }
}
