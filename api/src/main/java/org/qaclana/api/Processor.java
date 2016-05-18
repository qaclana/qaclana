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
package org.qaclana.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Basic contract for request/response processors. The firewall will run each processor twice, one once the request
 * first comes in, and again when the response is ready to be sent to the client.
 *
 * Implementations should self instantiate and register themselves with the {@link ProcessorRegistry}
 *
 * @see ProcessorRegistry
 * @author Juraci Paixão Kröhling
 */
public interface Processor {
    // TODO: we will need different types of processors, such as score-based processor, accept/reject processor

    FirewallOutcome process(HttpServletRequest request);
    FirewallOutcome process(HttpServletResponse response);
}
