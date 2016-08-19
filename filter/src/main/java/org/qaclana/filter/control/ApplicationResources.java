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
package org.qaclana.filter.control;

import org.qaclana.settings.SettingsValue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;

/**
 * Place where application resources are defined.
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ApplicationResources {
    @Inject
    @SettingsValue("qaclana.server.socket.endpoint")
    String serverSocketEndpoint;

    @Produces
    @SocketServerEndpointUri
    public URI getSocketServerEndpointUri() throws URISyntaxException {
        return new URI(serverSocketEndpoint);
    }

    @Produces
    public Clock getClock() {
        return Clock.systemUTC();
    }
}
