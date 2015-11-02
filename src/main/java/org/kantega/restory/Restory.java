package org.kantega.restory;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configurable;

/**
 *
 */

public class Restory implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    if (description.isTest()) {
                        SuiteRestory.exchangeMap.put(description.getClassName() + "::" + description.getMethodName(), Collector.lastExchange());
                    }
                }
            }
        };
    }


    public void configure(Configurable<ClientBuilder> configurable) {
        configurable.register(CollectingFilter.class);
    }

}
