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

import org.qaclana.api.entity.event.NewBlockedIpRange;
import org.qaclana.api.entity.event.RemovedBlockedIpRange;
import org.qaclana.api.entity.ws.BasicMessage;
import org.qaclana.api.entity.ws.BlockedIpRangeMessage;
import org.qaclana.api.entity.ws.UnblockedIpRangeMessage;
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
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class BlockedIpPropagator {
    @Inject
    @Firewall
    Instance<Map<String, Session>> firewallSessionsInstance;

    @Inject
    Event<SendMessage> sendMessageEvent;

    @Resource
    private ManagedExecutorService executor;

    @Asynchronous
    public void propagate(@Observes NewBlockedIpRange newBlockedIpRange) {
        BasicMessage message = new BlockedIpRangeMessage(newBlockedIpRange.getIpRange());
        firewallSessionsInstance.get().forEach((sessionId, session) ->
                executor.submit(
                        () -> sendMessageEvent.fire(new SendMessage(session, message))
                )
        );
    }

    @Asynchronous
    public void propagate(@Observes RemovedBlockedIpRange removedBlockedIpRange) {
        BasicMessage message = new UnblockedIpRangeMessage(removedBlockedIpRange.getIpRange());
        firewallSessionsInstance.get().forEach((sessionId, session) ->
                executor.submit(
                        () -> sendMessageEvent.fire(new SendMessage(session, message))
                )
        );
    }
}
