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
package org.qaclana.services.messagesender;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.*;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 10330, max = 10339)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10330, value = "Web Socket message didn't have a type in the payload. Payload: [%s]")
    void messageMissingType(String payload);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10331, value = "Could not convert the message to JSON. Message: [%s]. Reason: ")
    void failedToConvertMessageToJson(String message, @Cause Throwable t);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10332, value = "Sending message to [%s]. Message type: [%s]")
    void sendingMessageToDestination(String destination, String messageType);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10333, value = "Failed to send message to destination [%s]. Reason: ")
    void failedToSendMessageToDestination(String destination, @Cause Throwable t);
}
