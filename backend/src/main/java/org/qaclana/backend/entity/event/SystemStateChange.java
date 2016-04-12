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
package org.qaclana.backend.entity.event;

import org.qaclana.backend.entity.SystemState;

import java.util.UUID;

/**
 * CDI event fired when a system state changes.
 *
 * @author Juraci Paixão Kröhling
 */
public class SystemStateChange extends BasicEvent {
    private SystemState state;

    public SystemStateChange(SystemState state) {
        this.state = state;
    }

    public SystemStateChange(UUID id, SystemState state) {
        super(id);
        this.state = state;
    }

    public SystemState getState() {
        return state;
    }
}
