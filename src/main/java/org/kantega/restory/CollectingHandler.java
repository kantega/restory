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

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class CollectingHandler implements SOAPHandler<SOAPMessageContext> {

    @Resource
    private WebServiceContext webServiceContext;


    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        capture(context);
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        capture(context);
        return true;
    }


    private void capture(SOAPMessageContext context) {
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        StringWriter writer = new StringWriter();
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.transform(new DOMSource(context.getMessage().getSOAPPart()), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }


        if(outboundProperty) {
            System.out.println("Capturing SOAP request");
            CollectedRequest msg = new CollectedRequest();
            msg.setPayload(writer.toString());
            msg.setAddress((String) context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
            msg.setMethod("POST");
            Collector.newExchange(msg);
        } else {
            CollectedResponse msg = new CollectedResponse();
            msg.setPayload(writer.toString());
            msg.setResponseCode((Integer) context.get(MessageContext.HTTP_RESPONSE_CODE));
            msg.setResponseReason(Response.Status.fromStatusCode(msg.getResponseCode()).getReasonPhrase());
            msg.setHeaders((Map<String, List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS));
            Collector.endExchange(msg);
            System.out.println("Capturing SOAP response");
        }
    }


    @Override
    public void close(MessageContext context) {

    }
}
