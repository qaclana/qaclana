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

import org.qaclana.backend.control.Firewall;
import org.qaclana.backend.control.MsgLogger;
import org.qaclana.backend.entity.event.NewFirewallInstanceRegistered;
import org.qaclana.services.messagesender.SocketMessagePropagator;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;

/**
 * A Web Socket endpoint intended to be used between the server and firewall instances (proxies and/or filters)
 *
 * @author Juraci Paixão Kröhling
 */
@ServerEndpoint("/ws/instance")
@Stateless
public class FirewallSocket {
    private static final MsgLogger log = MsgLogger.LOGGER;
    @Inject
    Event<NewFirewallInstanceRegistered> newFirewallInstanceRegisteredEvent;
    @Inject
    SocketMessagePropagator socketMessagePropagator;
    @Inject
    @Firewall
    private Instance<Map<String, Session>> sessionsInstance;

    @OnOpen
    public void onOpen(Session session) {
        log.firewallSocketOpened();
        sessionsInstance.get().put(session.getId(), session);
        newFirewallInstanceRegisteredEvent.fire(new NewFirewallInstanceRegistered(session));
    }

    @OnMessage
    public void onMessage(Session session, String payload) {
        log.firewallSocketMessage(payload);
        socketMessagePropagator.propagate(payload);
    }

    @OnClose
    public void onClose(Session session) {
        log.firewallSocketClosed();
        sessionsInstance.get().remove(session.getId());
    }

}
