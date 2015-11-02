/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
