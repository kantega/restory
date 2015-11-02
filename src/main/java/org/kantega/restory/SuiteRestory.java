package org.kantega.restory;

import com.sun.tools.javadoc.Main;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.*;

/**
 *
 */

public class SuiteRestory implements TestRule {

    public static Map<String, CollectedExchange> exchangeMap = new HashMap<>();

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    if(description.isSuite()) {

                        File testOutputDirectory = new File(description.getTestClass().getResource("/").toURI().getPath());

                        File targetDirectory = testOutputDirectory.getParentFile();

                        File baseDirectory = targetDirectory.getParentFile();

                        File sourceDir = new File(baseDirectory, "src/test/java");


                        File destinationDir = new File(targetDirectory, "restory");

                        RestoryDoclet.setExchanges(exchangeMap);
                        RestoryDoclet.setDescription(description);

                        List<String> args = new ArrayList<>();

                        args.addAll(Arrays.asList("-doclet", RestoryDoclet.class.getName(),
                                "-dest", new File(destinationDir, description.getTestClass().getSimpleName() +".html").getAbsolutePath(),
                                "-docletpath", testOutputDirectory.getAbsolutePath()));


                        args.add(toSource(sourceDir, description.getTestClass()));

                        for (Description childDesciption : description.getChildren()) {
                            args.add(toSource(sourceDir, childDesciption.getTestClass()));
                        }

                        Main.execute("restory", args.toArray(new String[args.size()]));
                    }
                }
            }
        };
    }

    private String toSource(File sourceDir, Class<?> testClass) {
        return new File(sourceDir, testClass.getName().replace('.', '/') + ".java").getAbsolutePath();
    }


}
