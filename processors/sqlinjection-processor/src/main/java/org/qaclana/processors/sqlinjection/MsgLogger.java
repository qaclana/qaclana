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
package org.qaclana.processors.sqlinjection;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 10220, max = 10239)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10221, value = "IP Range added to the blacklist: [%s]")
    void addedIpRangeToBlacklist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10222, value = "IP Range removed from the blacklist: [%s]")
    void removedIpRangeFromBlacklist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10223, value = "IP Range found on the blacklist. Request's IP: [%s] , blacklisted range: [%s]")
    void ipRangeFoundOnBlacklist(String ipRangeFromRequest, String blacklistedIpRange);
}
