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
package org.qaclana.processors.blacklist;

import org.qaclana.api.entity.IpRange;

import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
public class BlacklistContainer {
    @Inject
    Event<BlacklistUpdated> blacklistUpdatedEvent;
    private List<IpRange> blacklistedIpRanges = Collections.synchronizedList(new ArrayList<>());

    public List<IpRange> getBlacklistedIpRanges() {
        return Collections.unmodifiableList(blacklistedIpRanges);
    }

    public boolean add(IpRange ipRange) {
        boolean result = this.blacklistedIpRanges.add(ipRange);
        blacklistUpdatedEvent.fire(new BlacklistUpdated(BlacklistUpdated.OperationType.ADDED, ipRange));
        return result;
    }

    public boolean remove(IpRange ipRange) {
        boolean result = this.blacklistedIpRanges.remove(ipRange);
        blacklistUpdatedEvent.fire(new BlacklistUpdated(BlacklistUpdated.OperationType.REMOVED, ipRange));
        return result;
    }
}
