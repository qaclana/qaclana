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
package org.qaclana.api.entity.ws;

import org.qaclana.api.SystemState;

/**
 * A message sent to be sent via web sockets with new system states.
 *
 * @author Juraci Paixão Kröhling
 */
public class SystemStateChangeMessage extends BasicMessage {
    public static final String EVENT_TYPE = "system-state-change";
    private SystemState state;

    public SystemStateChangeMessage(SystemState state) {
        this.state = state;
    }

    public SystemState getState() {
        return state;
    }

    public void setState(SystemState state) {
        this.state = state;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    @Override
    public String toString() {
        return "SystemStateChangeMessage{" +
                "state=" + state +
                '}';
    }
}
