package net.imglib2.meta.examples;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * A simple test that ensures the examples continue to run
 */
public class TestExamples {

    @Test
    public void testExamplesAreErrorFree() {
        // Suppress example output
        System.setOut(new PrintStream(new ByteArrayOutputStream()));

        List<Runnable> tests = Arrays.asList(
                () -> Example01Datasets.main(new String[0]),
                () -> Example02Metadata.main(new String[0]),
                () -> Example03VaryingMetadata.main(new String[0]),
                () -> Example04FluentViews.main(new String[0]),
                () -> Example05MissingMetadata.main(new String[0]),
                () -> Example06MutatingMetadata.main(new String[0]),
                () -> Example07Interfaces.main(new String[0])
        );

        for (Runnable test : tests) {
            try {
                test.run();
            } catch (Exception e) {
                Assert.fail("Example threw an exception: " + e.getMessage());
            }
        }
    }

}
