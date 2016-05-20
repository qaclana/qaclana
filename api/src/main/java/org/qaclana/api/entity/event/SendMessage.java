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
package org.qaclana.api.entity.event;

import org.qaclana.api.entity.ws.BasicMessage;

import javax.websocket.Session;
import java.util.UUID;

/**
 * CDI event fired when a component needs to send a message to web sockets.
 *
 * @author Juraci Paixão Kröhling
 */
public class SendMessage extends BasicEvent {
    private Session destination;
    private BasicMessage message;

    public SendMessage(Session destination, BasicMessage message) {
        this.destination = destination;
        this.message = message;
    }

    public SendMessage(UUID id, Session destination, BasicMessage message) {
        super(id);
        this.destination = destination;
        this.message = message;
    }

    public Session getDestination() {
        return destination;
    }

    public BasicMessage getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SendMessage{" +
                "destination=" + destination +
                ", message=" + message +
                '}';
    }
}
