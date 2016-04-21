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

import javax.ejb.Stateless;
import javax.websocket.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A web socket client for communicating with the server.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@ClientEndpoint
public class SocketClient {
    URI endpointURI = new URI("ws://localhost:8080/backend/v1/ws/instance");
    MsgLogger log = MsgLogger.LOGGER;

    public SocketClient() throws URISyntaxException {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            // TODO: the server is not available. Schedule a retry.
            log.cannotOpenSocketToServer(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.firewallSocketOpened();
    }

    @OnMessage
    public void onMessage(Session session, String payload) {
        log.firewallSocketMessage();
    }

    @OnClose
    public void onClose(Session session) {
        // TODO: schedule a retry.
        log.firewallSocketClosed();
    }
}
