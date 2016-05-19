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
package org.qaclana.processors.sqlinjection;

import org.qaclana.api.entity.Audit;
import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.AuditEventReported;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Reports an attack attempt to the server, so that it can eventually block the user if it so wishes.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@Asynchronous
public class AttackAttemptReporter {

    @Inject
    Event<AuditEventReported> auditEventReportedEvent;

    public void report(String requestId, String ipAddressAsString, String parameterName, String parameterValue, Pattern pattern) {
        BigInteger ipAddress = IpRange.fromString(ipAddressAsString).getStart();
        String message = String.format("Pattern [%s] violated for parameter [%s] with value [%s]", pattern.toString(), parameterName, parameterValue);
        Audit audit = new Audit(requestId, ipAddress, message);
        auditEventReportedEvent.fire(new AuditEventReported(audit));
    }
}
