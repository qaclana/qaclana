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
package org.qaclana.settings;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.*;

/**
 * @author Juraci Paixão Kröhling
 */
@MessageLogger(projectCode = "QCLN")
@ValidIdRange(min = 10300, max = 10319)
public interface MsgLogger {
    MsgLogger LOGGER = Logger.getMessageLogger(MsgLogger.class, MsgLogger.class.getPackage().getName());

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10300, value = "Loading settings from the configuration file at [%s].")
    void loadingSettingsFromFile(String file);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10301, value = "Loading settings from JNDI base path [%s].")
    void loadingSettingsFromJndi(String basePath);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10302, value = "It seems that the configuration file exists, but we couldn't read it. Reason: ")
    void reportProblemInReadingConfig(@Cause Throwable throwable);
}
