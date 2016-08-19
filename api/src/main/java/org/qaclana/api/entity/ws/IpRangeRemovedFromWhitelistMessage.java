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
package org.qaclana.api.entity.ws;

import org.qaclana.api.entity.IpRange;

/**
 * Web socket message broadcasting that a IP Range has been removed from the blacklist
 *
 * @author Juraci Paixão Kröhling
 */
public class IpRangeRemovedFromWhitelistMessage extends BasicMessage {
    public static final String EVENT_TYPE = "ip-range-removed-from-whitelist";
    private IpRange ipRange;

    public IpRangeRemovedFromWhitelistMessage(IpRange ipRange) {
        this.ipRange = ipRange;
    }

    public IpRange getIpRange() {
        return ipRange;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    @Override
    public String toString() {
        return "IpRangeRemovedFromWhitelistMessage{" +
                "ipRange=" + ipRange +
                '}';
    }
}
