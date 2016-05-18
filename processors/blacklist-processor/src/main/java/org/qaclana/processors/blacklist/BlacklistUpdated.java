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
package org.qaclana.processors.blacklist;

import org.qaclana.api.entity.IpRange;

/**
 * @author Juraci Paixão Kröhling
 */
public class BlacklistUpdated {
    enum OperationType {
        ADDED, REMOVED
    }

    private OperationType type;
    private IpRange ipRange;

    public BlacklistUpdated(OperationType type, IpRange ipRange) {
        this.type = type;
        this.ipRange = ipRange;
    }

    public OperationType getType() {
        return type;
    }

    public IpRange getIpRange() {
        return ipRange;
    }
}
