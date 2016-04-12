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

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.*;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 100000, max = 109999)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100000, value = "Web Socket opened for a Firewall worker.")
    void firewallSocketOpened();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100001, value = "A Firewall worker sent a message.")
    void firewallSocketMessage();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100002, value = "Web Socket closed for a Firewall worker.")
    void firewallSocketClosed();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100003, value = "Web Socket opened for a Frontend worker.")
    void frontendSocketOpened();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100004, value = "A Frontend worker sent a message.")
    void frontendSocketMessage();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100005, value = "Web Socket closed for a Frontend worker.")
    void frontendSocketClosed();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100006, value = "System state change request received. New state: [%s]")
    void systemStateChangeRequestReceived(String newState);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100007, value = "System state change applied to this system. New state: [%s]")
    void systemStateChangeApplied(String newState);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100008, value = "Sending message to [%s]. Message type: [%s]")
    void sendingMessageToDestination(String destination, String messageType);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100009, value = "Propagating system state change. New state: [%s]")
    void propagatingSystemStateChange(String newState);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100010, value = "qaclana initialized.")
    void applicationInitialized();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 100011, value = "qaclana is shutting down.")
    void applicationShuttingDown();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 100012, value = "Could not send close message to socket [%s]. Reason: ")
    void failedToProperlyCloseSession(String sessionId, @Cause Throwable t);
}
