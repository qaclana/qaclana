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
package org.qaclana.backend.control;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

/**
 * A context listener that prepares a required state before the application starts receiving requests
 * and before it finishes shutting down.
 *
 * @author Juraci Paixão Kröhling
 */
@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {
    private static final MsgLogger log = MsgLogger.LOGGER;

    @Inject
    @Firewall
    Instance<Map<String, Session>> firewallSessionsInstance;

    @Inject
    @Frontend
    Instance<Map<String, Session>> frontendSessionsInstance;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.applicationInitialized();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.applicationShuttingDown();
        closeAllSessions(firewallSessionsInstance.get());
        closeAllSessions(frontendSessionsInstance.get());
    }

    private void closeAllSessions(Map<String, Session> sessions) {
        sessions.forEach((sessionId, session) -> closeSession(session));
    }

    private void closeSession(Session session) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Server is shutting down."));
        } catch (IOException e) {
            log.failedToProperlyCloseSession(session.getId(), e);
        }
    }
}
