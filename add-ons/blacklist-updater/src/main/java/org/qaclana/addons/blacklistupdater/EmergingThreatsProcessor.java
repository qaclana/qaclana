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

import org.qaclana.api.entity.IpRange;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

/**
 * Batch processor for Emerging Threats blacklist.
 *
 * @author Juraci Paixão Kröhling
 */
@Named
public class EmergingThreatsProcessor implements ItemProcessor {
    @Override
    public Object processItem(Object item) throws Exception {
        // emerging threats' list is composed of comments (#), empty lines
        // and records. Records are either single IPs or CIDR. Examples:
        // 82.196.6.164
        // 1.4.0.0/17

        String line = (String) item;
        line = line.trim();
        if (line.startsWith("#") || line.isEmpty()) {
            return null;
        }
        return IpRange.fromString(line);
    }
}
