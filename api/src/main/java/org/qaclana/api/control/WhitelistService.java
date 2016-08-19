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
import org.qaclana.api.entity.event.IpRangeAddedToWhitelist;
import org.qaclana.api.entity.event.IpRangeRemovedFromWhitelist;

import java.util.List;

/**
 * A service that provides access to IP Ranges which are part of a whitelist. Implementations should take care to emit
 * the appropriate CDI events when operations are successful.
 *
 * @author Juraci Paixão Kröhling
 */
public interface WhitelistService {

    /**
     * Lists all known IP Ranges in the whitelist.
     *
     * @return a list of {@link IpRange} on the whitelist
     */
    List<IpRange> list();

    /**
     * Checks whether a given {@link IpRange} is in the whitelist
     *
     * @param ipRange the {@link IpRange} to be checked
     * @return true if the range is in the whitelist
     */
    boolean isInWhitelist(IpRange ipRange);

    /**
     * Adds the IP Range to the storage. If the operation is successful, the CDI event {@link IpRangeAddedToWhitelist}
     * is emitted.
     *
     * @param ipRange the {@link IpRange} to add to the whitelist
     */
    void add(IpRange ipRange);

    /**
     * Removes the IP Range from the storage. If the operation is successful, the CDI event
     * {@link IpRangeRemovedFromWhitelist} is emitted.
     *
     * @param ipRange the {@link IpRange} to remove from the whitelist
     */
    void remove(IpRange ipRange);
}
