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
package org.qaclana.api.entity;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an audit event, broadcasted via CDI event.
 *
 * @author Juraci Paixão Kröhling
 */
public class Audit {
    private UUID id;
    private String requestId;
    private BigInteger ipAddress;
    private String text;
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public Audit(UUID id, String requestId, BigInteger ipAddress, String text, ZonedDateTime timestamp) {
        this.id = id;
        this.requestId = requestId;
        this.ipAddress = ipAddress;
        this.text = text;
        this.timestamp = timestamp;
    }

    public Audit(String requestId, BigInteger ipAddress, String text) {
        this.id = UUID.randomUUID();
        this.requestId = requestId;
        this.ipAddress = ipAddress;
        this.text = text;
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

    public UUID getId() {
        return id;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Audit{" +
                "id=" + id +
                ", requestId='" + requestId + '\'' +
                ", ipAddress=" + ipAddress +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
