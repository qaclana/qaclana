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
package org.qaclana.filter.control;

import org.qaclana.filter.entity.IncomingHttpRequest;
import org.qaclana.filter.entity.OutgoingHttpResponse;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * An asynchronous EJB that fires a CDI event, for interested parties in recording the requests/responses.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
@Asynchronous
public class Recorder {

    @Inject
    Event<IncomingHttpRequest> incomingHttpRequestEvent;

    @Inject
    Event<OutgoingHttpResponse> outgoingHttpResponseEvent;

    public void record(ServletRequest request) {
        // we wrap the event into an async EJB because firing and processing of events is blocking
        incomingHttpRequestEvent.fire(new IncomingHttpRequest(request));
    }

    public void record(ServletResponse response) {
        // we wrap the event into an async EJB because firing and processing of events is blocking
        outgoingHttpResponseEvent.fire(new OutgoingHttpResponse(response));
    }
}
