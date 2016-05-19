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
@ValidIdRange(min = 10000, max = 10099)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10000, value = "Web Socket opened for a Firewall worker.")
    void firewallSocketOpened();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10001, value = "A Firewall worker sent a message: [%s]")
    void firewallSocketMessage(String message);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10002, value = "Web Socket closed for a Firewall worker.")
    void firewallSocketClosed();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10003, value = "Web Socket opened for a Frontend worker.")
    void frontendSocketOpened();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10004, value = "A Frontend worker sent a message.")
    void frontendSocketMessage();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10005, value = "Web Socket closed for a Frontend worker.")
    void frontendSocketClosed();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10006, value = "System state change request received. New state: [%s]")
    void systemStateChangeRequestReceived(String newState);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10007, value = "Reuse this.")
    void unused();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10008, value = "Reuse this.")
    void unused2();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10009, value = "Propagating system state change. New state: [%s]")
    void propagatingSystemStateChange(String newState);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10010, value = "qaclana initialized.")
    void applicationInitialized();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10011, value = "qaclana is shutting down.")
    void applicationShuttingDown();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10012, value = "Could not send close message to socket [%s]. Reason: ")
    void failedToProperlyCloseSession(String sessionId, @Cause Throwable t);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10013, value = "Could not convert the message to JSON. Message: [%s]. Reason: ")
    void failedToConvertMessageToJson(String message, @Cause Throwable t);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10014, value = "Failed to send message to destination [%s]. Reason: ")
    void failedToSendMessageToDestination(String destination, @Cause Throwable t);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10015, value = "Received a request to add new IP Range to the whitelist. IP Range: [%s]")
    void addIpRangeToWhitelist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10016, value = "Received a request to remove an IP Range from the whitelist. IP Range: [%s]")
    void removeIpRangeFromWhitelist(String ipRange);
}
