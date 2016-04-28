/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.qaclana.filter.control;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Place where application resources are defined.
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ApplicationResources {
    private static final MsgLogger log = MsgLogger.LOGGER;

    @Produces @SocketServerEndpointUri
    public URI getSocketServerEndpointUri() throws URISyntaxException {
        String uri = System.getProperty("org.qaclana.server.socket.endpoint", "ws://localhost:8080/backend/v1/ws/instance");
        return new URI(uri);
    }
}
