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

import org.qaclana.settings.SettingsValue;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Future;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class RecordLookupService {
    private static final String API_KEY_SETTINGS_NAME = "qaclana.projecthoneypot.apikey";
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    Event<IpFoundOnHoneyPotBlacklist> ipFoundOnHoneyPotBlacklistEvent;

    @Inject
    @SettingsValue(API_KEY_SETTINGS_NAME)
    String apiKey;

    @Asynchronous
    public Future<InetAddress[]> lookup(String ip) {
        if (null == apiKey) {
            logger.noApiKeyConfigured(API_KEY_SETTINGS_NAME);
            return new AsyncResult<>(null);
        }

        String host = String.format("%s.%s.dnsbl.httpbl.org", apiKey, formatIpForQuery(ip));

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
            ipFoundOnHoneyPotBlacklistEvent.fire(new IpFoundOnHoneyPotBlacklist(host));
        } catch (UnknownHostException e) {
            return new AsyncResult<>(null);
        }
        return new AsyncResult<>(addresses);
    }

    String formatIpForQuery(String ip) {
        if (null == ip) {
            return null;
        }

        // 213.109.53.16 becomes 16.53.109.213
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalStateException("Invalid IP: " + ip);
        }

        return String.format("%s.%s.%s.%s", parts[3], parts[2], parts[1], parts[0]);
    }
}
