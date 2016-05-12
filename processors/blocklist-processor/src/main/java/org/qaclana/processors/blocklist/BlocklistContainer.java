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
public class BlocklistContainer {
    private List<IpRange> blockedIpRanges = Collections.synchronizedList(new ArrayList<>());

    @Inject
    Event<BlocklistUpdated> blocklistUpdatedEvent;

    public List<IpRange> getBlockedIpRanges() {
        return Collections.unmodifiableList(blockedIpRanges);
    }

    public boolean add(IpRange ipRange) {
        boolean result = this.blockedIpRanges.add(ipRange);
        blocklistUpdatedEvent.fire(new BlocklistUpdated(BlocklistUpdated.OperationType.ADDED, ipRange));
        return result;
    }

    public boolean remove(IpRange ipRange) {
        boolean result = this.blockedIpRanges.remove(ipRange);
        blocklistUpdatedEvent.fire(new BlocklistUpdated(BlocklistUpdated.OperationType.REMOVED, ipRange));
        return result;
    }
}
