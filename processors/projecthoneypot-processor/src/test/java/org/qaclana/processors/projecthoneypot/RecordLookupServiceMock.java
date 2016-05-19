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
package org.qaclana.processors.projecthoneypot;

import javax.ejb.AsyncResult;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Juraci Paixão Kröhling
 */
@Specializes
@Singleton
public class RecordLookupServiceMock extends RecordLookupService {
    @Inject
    Event<IpFoundOnHoneyPotBlacklist> ipFoundOnHoneyPotBlacklistEvent;

    private static final List<String> ipsToReturnRecords = new ArrayList<>();

    static {
        ipsToReturnRecords.add("255.255.255.255");
    }

    public Future<InetAddress[]> lookup(String ip) {
        InetAddress[] addresses = null;
        if (ipsToReturnRecords.contains(ip)) {
            ipFoundOnHoneyPotBlacklistEvent.fire(new IpFoundOnHoneyPotBlacklist(ip));
            try {
                addresses = new InetAddress[]{InetAddress.getLocalHost()};
            } catch (UnknownHostException e) {
                return new AsyncResult<>(null);
            }
        }

        return new AsyncResult<>(addresses);
    }
}
