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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
@DependsOn("SocketClient") // otherwise, the socket client might get undeployed before this
public class SocketClientStarter {
    private Session session;
    private static final CloseReason UNDEPLOYMENT = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Client being undeployed.");
    MsgLogger log = MsgLogger.LOGGER;

    @Inject
    @SocketServerEndpointUri
    URI uri;

    @Inject
    SocketClient socketClient;

    @PostConstruct
    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(socketClient, uri);
        } catch (DeploymentException | IOException e) {
            log.cannotOpenSocketToServer(e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (null != session) {
                session.close(UNDEPLOYMENT);
            }
        } catch (Throwable t) {
            log.undeployingCloseFailed(t);
        }
    }
}
