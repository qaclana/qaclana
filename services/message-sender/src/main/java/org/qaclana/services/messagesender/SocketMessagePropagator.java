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
package org.qaclana.services.messagesender;

import org.qaclana.api.entity.event.NewSocketMessage;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@Asynchronous
public class SocketMessagePropagator {
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    Event<NewSocketMessage> newClientSocketMessageEvent;

    public void propagate(String payload) {
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        if (!object.containsKey("type")) {
            logger.messageMissingType(payload);
            throw new IllegalStateException("Unable to determine the message type for the socket message.");
        }

        String messageType = object.getString("type");
        newClientSocketMessageEvent.fire(new NewSocketMessage(messageType, payload));
    }
}
