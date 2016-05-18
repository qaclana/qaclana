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
package org.qaclana.services.jpa.control;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.*;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 10260, max = 10269)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10260, value = "IP Range [%s] is already in the blacklist. Skipping.")
    void ipRangeAlreadyInBlacklist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10261, value = "IP Range [%s] is already in the whitelist. Skipping.")
    void ipRangeAlreadyInWhitelist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10262, value = "IP Range [%s] is not in the blacklist. Skipping.")
    void ipRangeNotInBlacklist(String ipRange);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10263, value = "IP Range [%s] is not in the whitelist. Skipping.")
    void ipRangeNotInWhitelist(String ipRange);
}
