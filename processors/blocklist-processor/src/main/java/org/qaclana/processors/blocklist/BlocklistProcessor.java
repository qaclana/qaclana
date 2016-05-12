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

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;
import org.qaclana.api.entity.IpRange;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@Startup
public class BlocklistProcessor implements Processor {
    @Inject
    ProcessorRegistry registration;

    @Inject
    BlocklistContainer blocklistContainer;

    @PostConstruct
    public void register() {
        registration.register(this);
    }

    @Override
    public FirewallOutcome process(ServletRequest request) {
        // it's safe to assume that the IP from the request isn't a range, but we need to convert it into
        // a BigInteger, as it can be either IPv6 or IPv4. So, we just get a new IP Range, as it takes care
        // of all this calculation for us
        IpRange ipFromRequest = IpRange.fromString(request.getRemoteAddr());
        List<IpRange> blockedIpRanges = blocklistContainer.getBlockedIpRanges();
        boolean match = blockedIpRanges
                .stream()
                .anyMatch(blockedIpRange ->
                        ipFromRequest.getStart().compareTo(blockedIpRange.getStart()) >= 0
                                && ipFromRequest.getStart().compareTo(blockedIpRange.getEnd()) <= 0
                );

        if (match) {
            return FirewallOutcome.REJECT;
        }

        return FirewallOutcome.ACCEPT;
    }

    @Override
    public FirewallOutcome process(ServletResponse response) {
        return FirewallOutcome.ACCEPT;
    }
}
