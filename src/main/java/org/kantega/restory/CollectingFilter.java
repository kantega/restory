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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.*;
import java.nio.charset.Charset;

/**
 *
 */
public class CollectingFilter implements ClientRequestFilter, WriterInterceptor, ClientResponseFilter {

    private static final String ENTITY_LOGGER_PROPERTY = CollectingFilter.class.getName() + ".entityLogger";

    private static final int DEFAULT_MAX_ENTITY_SIZE = 8 * 1024;

    private final int maxEntitySize;

    public CollectingFilter() {
        this.maxEntitySize = DEFAULT_MAX_ENTITY_SIZE;
    }

    @Override
    public void filter(ClientRequestContext context) throws IOException {

        CollectedRequest msg = new CollectedRequest();

        msg.setMethod(context.getMethod());
        msg.setAddress( context.getUri().toString());
        msg.setHeaders( context.getStringHeaders());


        if (context.hasEntity()) {
            final OutputStream stream = new LoggingStream(msg, context.getEntityStream()) {
                @Override
                public void collect() {
                    Collector.newExchange(msg);
                }
            };
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            Collector.newExchange(msg);
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        final StringBuilder b = new StringBuilder();

        CollectedResponse msg = new CollectedResponse();


        msg.setResponseCode(responseContext.getStatus());
        msg.setResponseReason(responseContext.getStatusInfo().getReasonPhrase());
        msg.setHeaders( responseContext.getHeaders());

        if (responseContext.hasEntity()) {
            responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream(),
                    getCharset(responseContext.getMediaType())));
        }

        msg.setPayload(b.toString());

        Collector.endExchange(msg);


    }

    private InputStream logInboundEntity(final StringBuilder b, InputStream stream, final Charset charset) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = stream.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize), charset));
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            CollectedRequest msg = stream.getMsg();
            msg.setPayload(stream.getStringBuilder(getCharset(writerInterceptorContext.getMediaType())).toString());
            stream.collect();
        }
    }


    private abstract class LoggingStream extends FilterOutputStream {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final StringBuilder b;
        private final CollectedRequest msg;


        LoggingStream(final CollectedRequest msg, final OutputStream inner) {
            super(inner);
            this.msg = msg;
            b = new StringBuilder();

        }

        public CollectedRequest getMsg() {
            return msg;
        }

        StringBuilder getStringBuilder(final Charset charset) {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, maxEntitySize), charset));
            if (entity.length > maxEntitySize) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= maxEntitySize) {
                baos.write(i);
            }
            out.write(i);
        }

        public abstract void collect();
    }

    public static Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? Charset.forName("UTF-8"): Charset.forName(name);
    }
}
