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
package org.qaclana.api.entity.ws;

import org.qaclana.api.entity.Audit;

/**
 * Message flowing from the client to the server indicating a new audit event.
 *
 * @author Juraci Paixão Kröhling
 */
public class AuditEventMessage extends BasicMessage {
    public static final String EVENT_TYPE = "new-audit-event";
    private Audit audit;

    public AuditEventMessage(Audit audit) {
        this.audit = audit;
    }

    public Audit getAudit() {
        return audit;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }
}
