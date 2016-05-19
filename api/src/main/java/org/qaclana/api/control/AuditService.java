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
package org.qaclana.api.control;

import org.qaclana.api.entity.Audit;
import org.qaclana.api.entity.IpRange;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

/**
 * Service to handle persistent audit data.
 *
 * @author Juraci Paixão Kröhling
 */
public interface AuditService {
    /**
     * Lists all the audit events for a given request.
     * @param requestId    the request ID for the events
     * @return a list of {@link Audit} or null if no events are found.
     */
    List<Audit> listEventsForRequestId(String requestId);

    /**
     * Lists all the audit events for a given IP address. Note that IP addresses do change, so, even though multiple
     * events might report the same IP address, it doesn't necessarily mean that it's the same client. Still, it's
     * a good indication of a "bad network" if the same IP address presents the same behavior most of the time.
     * @param ipAddress    the IP address for the events
     * @return a list of {@link Audit} or null if no events are found
     */
    List<Audit> listEventsForClientIp(InetAddress ipAddress);

    /**
     * Lists all the audit events for a given IP range. Note that IP addresses do change, so, even though multiple
     * events might report the same IP address, it doesn't necessarily mean that it's the same client. Still, it's
     * a good indication of a "bad network" if the same IP address presents the same behavior most of the time.
     * @param ipRange    the IP range to get the audit events for
     * @return a list of {@link Audit} or null if no events are found
     */
    List<Audit> listEventsForClientIp(IpRange ipRange);

    /**
     * Stores a new audit event.
     * @param audit    the {@link Audit} event to be added
     */
    void add(Audit audit);

    /**
     * Removes a previously stored audit event
     * @param id    the audit event's ID
     */
    void remove(UUID id);

    /**
     * Retrieve a single audit based on its ID
     * @param id    the ID for the existing audit event
     * @return the audit event or null if none exists under the ID
     */
    Audit get(UUID id);
}
