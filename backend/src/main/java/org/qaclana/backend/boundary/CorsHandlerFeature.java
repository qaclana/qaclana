/*
 * Copyright 2016 Juraci Paixão Kröhling
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
package org.qaclana.backend.boundary;

import org.qaclana.backend.control.MsgLogger;
import org.qaclana.settings.SettingsValue;

import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * @author Juraci Paixão Kröhling
 */
@Provider
public class CorsHandlerFeature implements DynamicFeature {
    private static final MsgLogger log = MsgLogger.LOGGER;
    private static final CorsRequestHandler FILTER = new CorsRequestHandler();

    @Inject
    @SettingsValue("qaclana.server.cors.enabled")
    String corsEnabled;

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if ("true".equalsIgnoreCase(corsEnabled)) {
            context.register(FILTER);
            log.corsEnabled();
        }
    }
}
