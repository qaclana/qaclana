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

/**
 * CDI event emitted when a new message is to be sent to a client socket, such as firewall instances.
 *
 * @author Juraci Paixão Kröhling
 */
public class NewSocketMessage extends BasicEvent {
    private String type;
    private String message;

    public NewSocketMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NewSocketMessage{" +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
