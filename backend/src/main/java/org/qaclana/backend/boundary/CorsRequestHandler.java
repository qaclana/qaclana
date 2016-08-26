/*
 * Copyright 2016 Juraci Paixão Kröhling
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qaclana.backend.boundary;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * @author Juraci Paixão Kröhling
 */
public class CorsRequestHandler implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null) {
            return;
        }

        // TODO: think if we want to allow a custom configuration for the Cors
        // if so, we might want to remove the WARN from the log, in case it's enabled
        MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
        responseHeaders.add("Access-Control-Allow-Origin", origin);
        responseHeaders.add("Access-Control-Allow-Credentials", "true");
        responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseHeaders.add("Access-Control-Max-Age", 60 * 60);
        responseContext.setStatus(200);
    }
}
