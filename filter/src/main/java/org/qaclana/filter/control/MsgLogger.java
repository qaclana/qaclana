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
package org.qaclana.filter.control;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.*;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 10100, max = 10199)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10100, value = "Web Socket opened. Ready to get information from the server. My ID: [%s]")
    void firewallSocketOpened(String id);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10101, value = "Got a message via Web Socket from the server.")
    void firewallSocketMessage();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10102, value = "Web Socket closed. Reason: [%s]")
    void firewallSocketClosed(String reason);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10103, value = "Firewall told us to not process this request.")
    void stopProcessingRequest();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10104, value = "Firewall filter initialized.")
    void filterInitialized();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10105, value = "Firewall filter destroyed.")
    void filterDestroyed();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10106, value = "Firewall timed out waiting for the request analyzer's outcome.")
    void timeoutAnalyzingRequest();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10107, value = "Firewall timed out waiting for the response analyzer's outcome.")
    void timeoutAnalyzingResponse();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10108, value = "Cannot open client socket to the server.")
    void cannotOpenSocketToServer(@Cause Throwable throwable);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10109, value = "Recording request data for request [%s].")
    void recordingRequest(String requestId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10110, value = "Recording response data for request [%s].")
    void recordingResponse(String requestId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10111, value = "Starting to send data to processors [%s].")
    void startQueueingProcessors(int size);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10112, value = "Processor [%s] started for request [%s].")
    void startProcessor(String name, String requestId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10113, value = "Processor [%s] finished for request [%s] with outcome [%s].")
    void finishedProcessor(String name, String requestId, String outcome);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10114, value = "Waiting for processors of request [%s] to fininsh.")
    void waitingForProcessors(String requestId);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10115, value = "Interrupted while waiting for processors of request [%s].")
    void waitInterruptedForProcessors(String requestId, @Cause Throwable throwable);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10116, value = "Finished waiting for processors of request [%s].")
    void finishedWaitingForProcessors(String requestId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10117, value = "Waiting for outcome of request [%s].")
    void waitingForOutcomeOfRequest(String requestId);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10118, value = "Finished waiting for outcome of request [%s].")
    void finishedWaitingForOutcomeOfRequest(String requestId);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10119, value = "Interrupted while waiting for outcome of request [%s].")
    void waitInterruptedForOutcome(String requestId, @Cause Throwable throwable);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10120, value = "Final outcome for request [%s]: [%s].")
    void finalOutcomeForRequest(String requestId, String outcome);

    @LogMessage(level = Logger.Level.FATAL)
    @Message(id = 10121, value = "Cannot process a request/response pair without a request.")
    void cannotAcceptProcessingWithoutRequest();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10122, value = "Execution of %s#%s() took %dms to complete.")
    void overheadReporter(String className, String methodName, long nanoSeconds);
}
