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

import com.sun.javadoc.*;
import org.junit.runner.Description;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class RestoryDoclet {
    private static Map<String, CollectedExchange> exchanges;
    private static Description description;

    private static Set<String> ignoredRequestHeaders = new HashSet<>();
    private static Set<String> ignoredResponseHeaders = new HashSet<>();

    static {
        ignoredRequestHeaders.add("user-agent");
        ignoredResponseHeaders.add("server");
        ignoredResponseHeaders.add("date");
    }

    public static boolean start(RootDoc root) throws IOException {

        File destFile = getDestFile(root.options());
        destFile.getParentFile().mkdirs();

        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFile), "utf-8"))) {

            w.println("<html>");

            w.println("<head>");
            w.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" integrity=\"sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ==\" crossorigin=\"anonymous\">");
            w.println("</head>");
            w.println("<body>");
            w.println("<div class=container>");
            ClassDoc[] classes = root.classes();


            for (int i = 0; i < classes.length; ++i) {
                ClassDoc classDoc = classes[i];
                if(isSuiteClass(classDoc)) {
                    printTitles(w, classDoc, 1);
                    w.println("<p>");
                    w.println(classDoc.commentText());
                    w.println("</p>");
                }
            }

            printTableOfContents(w, classes);

            for (int i = 0; i < classes.length; ++i) {
                ClassDoc classDoc = classes[i];

                if(isSuiteClass(classDoc)) {
                    continue;
                }
                printTitles(w, classDoc, 2);

                w.println("<p>");
                w.println(classDoc.commentText());
                w.println("</p>");

                for (MethodDoc methodDoc : classDoc.methods()) {
                    String methodKey = methodDoc.containingClass().qualifiedName() + "::" + methodDoc.name();
                    w.println("<a name='" + methodKey +"'></a>");

                    printTitles(w, methodDoc, 3);
                    w.println("<p>");
                    w.println(methodDoc.commentText());
                    w.println("</p>");

                    CollectedExchange exchange = exchanges.get(methodDoc.containingClass().qualifiedName() +"::" +methodDoc.name());
                    if(exchange != null) {

                        w.println("<p><code>");
                        w.println(exchange.getRequest().getMethod() + " " + exchange.getRequest().getAddress());
                        w.println("</code></p>");
                        if(exchange.getRequest().getHeaders() != null) {
                            w.println("<pre>");
                            printHeaders(w, exchange.getRequest().getHeaders(), ignoredRequestHeaders);
                            w.println("</pre>");
                        }
                        if(exchange.getRequest().getPayload() != null) {
                            pre(w, exchange.getRequest().getPayload());
                        }

                        if(exchange.getResponse().getHeaders() != null) {
                            w.println("<pre>");
                            w.print("<div class='label ");
                            int code = exchange.getResponse().getResponseCode();
                            if(code >= 200 && code < 300) {
                                w.print("label-success'");
                            } else if(code >= 400 && code < 500) {
                                w.print("label-warning'");
                            } else if(code >= 500 && code < 600) {
                                w.print("label-warning'");
                            } else {
                                w.print("label-default'");
                            }

                            w.print(">");

                            w.print(code);
                            w.print(" " + exchange.getResponse().getResponseReason());

                            w.println("</div>");
                            printHeaders(w, exchange.getResponse().getHeaders(), ignoredResponseHeaders);
                            w.println("</pre>");
                        }
                        if(exchange.getResponse().getPayload() != null) {
                            pre(w, exchange.getResponse().getPayload());
                        }
                    }
                }
            }

            w.println("</div>");
            w.println("</body>");

            w.println("</html>");
        }

        return true;
    }

    private static void printTableOfContents(PrintWriter w, ClassDoc[] classes) {
        w.println("<ul>");

        for (ClassDoc classDoc : classes) {
            if(!isSuiteClass(classDoc)) {
                w.println("<li>");
                w.println("<a href='#" +classDoc.qualifiedName() + "'>");
                w.print(classDoc.tags("title")[0].text());
                w.println("</a>");

                w.println("<ul>");
                for (MethodDoc methodDoc : classDoc.methods()) {

                    String methodKey = methodDoc.containingClass().qualifiedName() + "::" + methodDoc.name();
                    CollectedExchange exchange = exchanges.get(methodKey);
                    if(exchange != null) {
                        w.println("<li>");
                        w.println("<a href='#" + methodKey + "'>");
                        for (Tag title : methodDoc.tags("title")) {
                            w.print(title.text());
                        }
                        w.println("</a>");
                        w.println("</li>");
                    }

                }
                w.println("</ul>");
                w.println("</li>");
            }
        }
        w.println("</ul>");
    }

    private static boolean isSuiteClass(ClassDoc classDoc) {
        return classDoc.qualifiedName().equals(description.getClassName());
    }

    private static void printHeaders(PrintWriter w, Map<String, List<String>> headers, Set<String> ignoredHeaders) {

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if(entry.getKey() != null && ! ignoredHeaders.contains(entry.getKey().toLowerCase())) {
                for (String value : entry.getValue()) {
                    w.println(entry.getKey() + ": " + value);
                }
            }
        }

    }

    private static void pre(PrintWriter w, String payload) {
        w.println("<pre>");
        w.print(payload.replace("<","&lt;").replace(">", "&gt;"));
        w.println("</pre>");
    }

    private static void printTitles(PrintWriter w, Doc doc, int level) {
        Tag[] titles = doc.tags("title");
        for (Tag title : titles) {
            w.println("<h" + level +">");
            w.print(title.text());
            w.println("</h" + level + ">");
        }
    }

    private static File getDestFile(String[][] options) {
        for (int i = 0; i < options.length; i++) {
            String[] option = options[i];
            if("-dest".equals(option[0])) {
                return new File(option[1]);
            }
        }
        return null;
    }

    public static int optionLength(String option) {
        if(option.equals("-dest")) {
            return 2;
        }
        return 0;
    }

    public static void setExchanges(Map<String, CollectedExchange> exchanges) {
        RestoryDoclet.exchanges = exchanges;
    }

    public static void setDescription(Description description) {
        RestoryDoclet.description = description;
    }
}