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

import org.qaclana.backend.control.Frontend;
import org.qaclana.backend.control.MsgLogger;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;

/**
 * Web Socket intended to be used for communication between the server and the admin UI frontend.
 *
 * @author Juraci Paixão Kröhling
 */
@ServerEndpoint("/ws/frontend")
@Stateless
public class FrontendSocket {
    private static final MsgLogger log = MsgLogger.LOGGER;

    @Inject
    @Frontend
    private Instance<Map<String, Session>> sessionsInstance;

    @OnOpen
    public void onOpen(Session session) {
        log.frontendSocketOpened();
        sessionsInstance.get().put(session.getId(), session);
    }

    @OnMessage
    public void onMessage(Session session, String payload) {
        log.frontendSocketMessage();
    }

    @OnClose
    public void onClose(Session session) {
        log.frontendSocketClosed();
        sessionsInstance.get().remove(session.getId());
    }
}
