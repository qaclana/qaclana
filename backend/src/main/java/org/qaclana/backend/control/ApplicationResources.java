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

import org.qaclana.api.SystemStateContainer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * Place where application resources are defined.
 *
 * @author Juraci Paixão Kröhling
 */
@ApplicationScoped
public class ApplicationResources {
    private static final MsgLogger log = MsgLogger.LOGGER;

    private Map<String, Session> frontendSessions = new HashMap<>();
    private Map<String, Session> firewallSessions = new HashMap<>();

    @Inject
    Instance<SystemStateContainer> systemStateContainerInstance;

    @Produces @Frontend @ApplicationScoped
    public Map<String, Session> getFrontendSessions() {
        return frontendSessions;
    }

    @Produces @Firewall @ApplicationScoped
    public Map<String, Session> getFirewallSessions() {
        return firewallSessions;
    }
}