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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configurable;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;

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

    public void configure(Binding binding) {

        List<Handler> handlerChain = new ArrayList<>();
        List<Handler> existingHandlers = binding.getHandlerChain();
        if(existingHandlers != null) {
            handlerChain.addAll(existingHandlers);
        }

        handlerChain.add(new CollectingHandler());

        binding.setHandlerChain(handlerChain);
    }
}
