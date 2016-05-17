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
package org.qaclana.filter.entity;

import javax.websocket.CloseReason;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Juraci Paixão Kröhling
 */
public class ConnectToSocketServer {
    private CloseReason reason;
    private long nextAttempt;
    private AtomicInteger attempt = new AtomicInteger(0);

    public ConnectToSocketServer(CloseReason reason, long nextAttempt) {
        this.reason = reason;
        this.nextAttempt = nextAttempt;
    }

    public CloseReason getReason() {
        return reason;
    }

    public long getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(long nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public void increaseAttempt() {
        attempt.incrementAndGet();
    }

    public int getAttempt() {
        return attempt.get();
    }
}
