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
package org.qaclana.processors.blocklist;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.*;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 10220, max = 10239)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10220, value = "Invalid Message. For a 'New Blocked IP Range' message, we expect the start and end of the range. Message: [%s]")
    void invalidMessage(String payload);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10221, value = "IP Range added to the blocklist: [%s]")
    void addedIpRangeToBlocklist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10222, value = "IP Range removed from the blocklist: [%s]")
    void removedIpRangeFromBlocklist(String ipRange);
}