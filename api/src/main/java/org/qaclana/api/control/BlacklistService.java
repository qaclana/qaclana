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
package org.qaclana.api.control;

import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.IpRangeAddedToBlacklist;
import org.qaclana.api.entity.event.IpRangeRemovedFromBlacklist;

import java.util.List;

/**
 * A service that provides access to IP Ranges which are part of a blacklist. Implementations should take care to emit
 * the appropriate CDI events when operations are successful.
 *
 * @author Juraci Paixão Kröhling
 */
public interface BlacklistService {
    /**
     * Lists all known IP Ranges in the blacklist.
     *
     * @return a list of {@link IpRange} on the blacklist
     */
    List<IpRange> list();

    /**
     * Checks whether a given {@link IpRange} is in the blacklist
     *
     * @param ipRange the {@link IpRange} to be checked
     * @return true if the range is in the blacklist
     */
    boolean isInBlacklist(IpRange ipRange);

    /**
     * Adds the IP Range to the storage. If the operation is successful, the CDI event {@link IpRangeAddedToBlacklist}
     * is emitted.
     *
     * @param ipRange the {@link IpRange} to add to the blacklist
     */
    void add(IpRange ipRange);

    /**
     * Removes the IP Range from the storage. If the operation is successful, the CDI event
     * {@link IpRangeRemovedFromBlacklist} is emitted.
     *
     * @param ipRange the {@link IpRange} to remove from the blacklist
     */
    void remove(IpRange ipRange);
}
