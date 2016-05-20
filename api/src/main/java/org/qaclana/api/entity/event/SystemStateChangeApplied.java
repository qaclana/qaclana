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
package org.qaclana.api.entity.event;

import org.qaclana.api.SystemState;

import java.util.UUID;

/**
 * CDI event fired when a system state changes.
 *
 * @author Juraci Paixão Kröhling
 */
public class SystemStateChangeApplied extends BasicEvent {
    private SystemState state;

    public SystemStateChangeApplied(SystemState state) {
        this.state = state;
    }

    public SystemStateChangeApplied(UUID id, SystemState state) {
        super(id);
        this.state = state;
    }

    public SystemState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "SystemStateChangeApplied{" +
                "state=" + state +
                '}';
    }
}
