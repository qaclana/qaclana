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
package org.qaclana.processors.whitelist;

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;
import org.qaclana.api.entity.IpRange;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@Startup
public class WhitelistProcessor implements Processor {
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    ProcessorRegistry registration;

    @Inject
    WhitelistContainer whitelistContainer;

    @PostConstruct
    public void register() {
        registration.register(this);
    }

    @Override
    public FirewallOutcome process(HttpServletRequest request) {
        IpRange ipFromRequest = IpRange.fromString(request.getRemoteAddr());
        List<IpRange> ipRangesOnWhitelist = whitelistContainer.getIpRangesOnWhitelist();
        Optional<IpRange> possibleIpRange = ipRangesOnWhitelist
                .stream()
                .filter(ipRangeOnWhitelist ->
                        ipFromRequest.getStart().compareTo(ipRangeOnWhitelist.getStart()) >= 0
                                && ipFromRequest.getStart().compareTo(ipRangeOnWhitelist.getEnd()) <= 0
                )
                .findAny();

        if (possibleIpRange.isPresent()) {
            IpRange ipRange = possibleIpRange.get();
            logger.ipRangeFoundOnWhitelist(ipFromRequest.toString(), ipRange.toString());
            return FirewallOutcome.ACCEPT;
        }

        return FirewallOutcome.NEUTRAL;
    }

    @Override
    public FirewallOutcome process(HttpServletResponse response) {
        return FirewallOutcome.NEUTRAL;
    }
}
