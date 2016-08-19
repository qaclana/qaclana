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
package org.qaclana.services.jpa.entity;

import org.qaclana.api.entity.Audit;

import javax.persistence.Entity;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Juraci Paixão Kröhling
 */
@Entity
public class AuditEntity extends QaclanaEntity {
    private String requestId;
    private BigInteger ipAddress;
    private String text;
    private ZonedDateTime timestamp;

    protected AuditEntity() {
    }

    public AuditEntity(Audit audit) {
        super(audit.getId());
        this.requestId = audit.getRequestId();
        this.ipAddress = audit.getIpAddress();
        this.text = audit.getText();
        this.timestamp = audit.getTimestamp();
    }

    public AuditEntity(UUID id, String requestId, BigInteger ipAddress, String text, ZonedDateTime timestamp) {
        super(id);
        this.requestId = requestId;
        this.ipAddress = ipAddress;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public BigInteger getIpAddress() {
        return ipAddress;
    }

    public String getText() {
        return text;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public Audit toAudit() {
        return new Audit(getId(), requestId, ipAddress, text, timestamp);
    }

    @Override
    public String toString() {
        return "AuditEntity{" +
                "id='" + getId() + '\'' +
                ", requestId='" + requestId + '\'' +
                ", ipAddress=" + ipAddress +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                "} ";
    }
}
