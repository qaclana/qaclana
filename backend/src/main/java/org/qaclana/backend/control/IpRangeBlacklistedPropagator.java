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

import org.qaclana.api.control.BlacklistService;
import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.IpRangeAddedToBlacklist;
import org.qaclana.api.entity.event.IpRangeRemovedFromBlacklist;
import org.qaclana.api.entity.ws.BasicMessage;
import org.qaclana.api.entity.ws.IpRangeAddedToBlacklistMessage;
import org.qaclana.api.entity.ws.IpRangeRemovedFromBlacklistMessage;
import org.qaclana.backend.entity.event.NewFirewallInstanceRegistered;
import org.qaclana.backend.entity.event.SendMessage;

import javax.annotation.Resource;
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
 * Propagates the information about the blacklisting of a specific {@link IpRange}.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class IpRangeBlacklistedPropagator {
    @Inject
    @Firewall
    Instance<Map<String, Session>> firewallSessionsInstance;

    @Inject
    Event<SendMessage> sendMessageEvent;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    BlacklistService blacklistService;

    /**
     * Builds a new Web Socket message and sends to all firewall instances with open sockets with us. Uses a
     * {@link ManagedExecutorService} to execute the submissions.
     *
     * @param ipRangeAddedToBlacklist    the new blacklisted {@link IpRange}
     */
    @Asynchronous
    public void propagate(@Observes IpRangeAddedToBlacklist ipRangeAddedToBlacklist) {
        BasicMessage message = new IpRangeAddedToBlacklistMessage(ipRangeAddedToBlacklist.getIpRange());
        firewallSessionsInstance.get().forEach((sessionId, session) ->
                executor.submit(
                        () -> sendMessageEvent.fire(new SendMessage(session, message))
                )
        );
    }

    /**
     * Builds a new Web Socket message and sends to all firewall instances with open sockets with us. Uses a
     * {@link ManagedExecutorService} to execute the submissions.
     *
     * @param ipRangeRemovedFromBlacklist    the {@link IpRange} that has been removed from the blacklist
     */
    @Asynchronous
    public void propagate(@Observes IpRangeRemovedFromBlacklist ipRangeRemovedFromBlacklist) {
        BasicMessage message = new IpRangeRemovedFromBlacklistMessage(ipRangeRemovedFromBlacklist.getIpRange());
        firewallSessionsInstance.get().forEach((sessionId, session) ->
                executor.submit(
                        () -> sendMessageEvent.fire(new SendMessage(session, message))
                )
        );
    }

    /**
     * Sends the blacklist to new firewall instances
     * @param newFirewallInstanceRegistered    the newly registered firewall instance
     */
    @Asynchronous
    public void propagate(@Observes NewFirewallInstanceRegistered newFirewallInstanceRegistered) {
        blacklistService.list().stream().forEach(ipRange -> {
            BasicMessage message = new IpRangeAddedToBlacklistMessage(ipRange);
            executor.submit(
                    () -> sendMessageEvent.fire(new SendMessage(newFirewallInstanceRegistered.getSession(), message))
            );
        });
    }
}
