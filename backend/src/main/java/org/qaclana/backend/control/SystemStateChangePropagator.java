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
package org.qaclana.backend.control;

import org.qaclana.backend.entity.event.NewFirewallInstanceRegistered;
import org.qaclana.backend.entity.event.SendMessage;
import org.qaclana.backend.entity.event.SystemStateChange;
import org.qaclana.backend.entity.ws.BasicMessage;
import org.qaclana.backend.entity.ws.SystemStateChangeMessage;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import java.util.Map;

/**
 * Propagates a system state change to interested parties.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@RolesAllowed("admin")
public class SystemStateChangePropagator {
    private static final MsgLogger log = MsgLogger.LOGGER;

    @Resource
    private ManagedExecutorService executor;

    @Inject @Firewall
    Instance<Map<String, Session>> firewallSessionsInstance;

    @Inject @Frontend
    Instance<Map<String, Session>> frontendSessionsInstance;

    @Inject
    Event<SendMessage> sendMessageEvent;

    @Inject
    SystemStateContainer systemStateInstance;

    @Asynchronous
    public void propagate(@Observes SystemStateChange changeEvent) {
        log.propagatingSystemStateChange(changeEvent.getState().name());
        BasicMessage message = new SystemStateChangeMessage(changeEvent.getState());
        propagateTo(frontendSessionsInstance.get(), message);
        propagateTo(firewallSessionsInstance.get(), message);
    }

    @Asynchronous
    public void propagate(@Observes NewFirewallInstanceRegistered newFirewallInstanceRegistered) {
        BasicMessage message = new SystemStateChangeMessage(systemStateInstance.getState());
        executor.submit(
                () -> sendMessageEvent.fire(new SendMessage(newFirewallInstanceRegistered.getSession(), message))
        );
    }

    private void propagateTo(Map<String, Session> sessions, BasicMessage message) {
        sessions.forEach((sessionId, session) ->
                executor.submit(
                        () -> sendMessageEvent.fire(new SendMessage(session, message))
                )
        );
    }
}
