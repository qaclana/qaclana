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
package org.qaclana.processors.projecthoneypot;

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
public class ProjectHoneyPotProcessor implements Processor {
    private static final MsgLogger logger = MsgLogger.LOGGER;
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    @Inject
    ProcessorRegistry registry;

    @Inject
    RecordLookupService recordLookupService;

    @PostConstruct
    public void register() {
        // TODO: we might first check if we have an API key before registering
        registry.register(this);
    }

    @Override
    public FirewallOutcome process(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!IPV4_PATTERN.matcher(ip).matches()) {
            // the project honey pot doesn't support IPv6 yet
            return FirewallOutcome.NEUTRAL;
        }

        try {
            InetAddress[] results = recordLookupService.lookup(ip).get(100, TimeUnit.MILLISECONDS);
            if (null != results && results.length > 0) {
                // we got a record!
                return FirewallOutcome.REJECT;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            logger.timedOut(ip);
            return FirewallOutcome.NEUTRAL;
        }

        return FirewallOutcome.NEUTRAL;
    }

    @Override
    public FirewallOutcome process(HttpServletResponse response) {
        return FirewallOutcome.NEUTRAL;
    }
}
