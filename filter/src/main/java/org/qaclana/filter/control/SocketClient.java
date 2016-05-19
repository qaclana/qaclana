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

import org.qaclana.api.SystemStateContainer;
import org.qaclana.filter.entity.ConnectToSocketServer;
import org.qaclana.services.messagensender.SocketMessagePropagator;

import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.*;

import static org.qaclana.filter.control.SocketClientStarter.UNDEPLOYMENT;

/**
 * A web socket client for communicating with the server.
 *
 * @author Juraci Paixão Kröhling
 */
@ClientEndpoint
@Singleton
public class SocketClient {
    MsgLogger log = MsgLogger.LOGGER;

    @Inject
    SystemStateContainer systemStateContainer;

    @Inject
    SocketMessagePropagator socketMessagePropagator;

    @Inject
    Event<ConnectToSocketServer> connectToSocketServerEvent;

    @OnOpen
    public void onOpen(Session session) {
        log.firewallSocketOpened(session.getId());
    }

    @OnMessage
    public void onMessage(String payload) {
        log.firewallSocketMessage(payload);
        socketMessagePropagator.propagate(payload);
    }

    @OnClose
    public void onClose(CloseReason reason) {
        // it's bad, but we compare both the close code and the reason phases, we the instance object for the reason
        // might not be the same
        if (!(UNDEPLOYMENT.getCloseCode().equals(reason.getCloseCode()) &&
                UNDEPLOYMENT.getReasonPhrase().equals(reason.getReasonPhrase()))) {
            // if *we* are going away, we don't want to try to connect to the server again
            // otherwise, we want to start retrying right
            connectToSocketServerEvent.fire(new ConnectToSocketServer(reason, System.currentTimeMillis()));
        }
        log.firewallSocketClosed(reason.toString());
    }
}
