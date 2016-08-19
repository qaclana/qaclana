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
package org.qaclana.services.auditpropagator;

import org.qaclana.api.control.AuditService;
import org.qaclana.api.entity.Audit;
import org.qaclana.api.entity.event.NewSocketMessage;
import org.qaclana.api.entity.ws.AuditEventMessage;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@Asynchronous
public class AuditEventRecorder {
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    AuditService auditService;

    public void record(@Observes NewSocketMessage newSocketMessage) {
        if (!AuditEventMessage.EVENT_TYPE.equals(newSocketMessage.getType())) {
            return;
        }

        JsonReader reader = Json.createReader(new StringReader(newSocketMessage.getMessage()));
        JsonObject object = reader.readObject();
        JsonObject auditJson = object.getJsonObject("audit");
        UUID id = UUID.fromString(auditJson.getString("id"));
        String requestId = auditJson.getString("requestId");
        BigInteger ipAddress = auditJson.getJsonNumber("ipAddress").bigIntegerValue();
        String text = auditJson.getString("text");
        ZonedDateTime timestamp = ZonedDateTime.parse(auditJson.getString("timestamp"));
        Audit audit = new Audit(id, requestId, ipAddress, text, timestamp);
        logger.auditEventReceived(audit.toString());
        auditService.add(audit);
    }

}
