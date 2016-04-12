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

import org.qaclana.backend.entity.event.SendMessage;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;

/**
 * Sends messages to socket destinations.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@RolesAllowed("admin")
public class MessageSender {
    private static final MsgLogger log = MsgLogger.LOGGER;

    @Asynchronous
    public void send(@Observes SendMessage event) {
        log.sendingMessageToDestination(event.getDestination().getId(), event.getMessage().getType());
        // TODO: send the message!
    }
}
