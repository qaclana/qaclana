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
package org.qaclana.addons.blacklistupdater;

import org.qaclana.addons.blacklistupdater.entity.EmergingThreats;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Produces an {@link InputStream} with the contents of the blacklist. This allows for tests to use alternative
 * implementations.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class EmergingThreatsInputStreamProvider {
    private static final String BLOCK_LIST = "https://rules.emergingthreats.net/fwrules/emerging-Block-IPs.txt";

    @Produces
    @EmergingThreats
    public InputStream produceEmergingThreatsInputStream() throws IOException {
        return new URL(BLOCK_LIST).openStream();
    }
}
