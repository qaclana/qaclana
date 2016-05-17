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

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * This is the backbone for the client, providing methods for the processing of the requests and responses.
 *
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class Firewall {
    public static final String HTTP_HEADER_REQUEST_ID = "Firewall-RequestID";

    MsgLogger log = MsgLogger.LOGGER;

    @Inject
    Recorder recorder;

    /**
     * A list of all processors known to us.
     */
    @Inject
    List<Processor> processors;

    /**
     * We delegate to the container as much as we can, for async operations.
     */
    @Resource
    private ManagedExecutorService executor;

    /**
     * Sends the {@link ServletRequest} for asynchronous processing.
     *
     * @param request    the request to be processed by all {@link Processor}
     * @return a {@link Future} that provides the {@link FirewallOutcome} once the processing is done.
     * @see #process(ServletRequest)
     * @see #doProcess(ServletRequest, ServletResponse)
     */
    public Future<FirewallOutcome> processAsync(ServletRequest request) {
        return executor.submit(() -> doProcess(request, null));
    }

    /**
     * Sends the {@link ServletResponse} for asynchronous processing.
     *
     * @param request     the request, for reference should the processor need it. Should not be changed.
     * @param response    the response, for analysis. Can be changed/rewritten by processors.
     * @return a {@link Future} that provides the {@link FirewallOutcome} once the processing is done.
     * @see #process(ServletRequest, ServletResponse)
     * @see #doProcess(ServletRequest, ServletResponse)
     */
    public Future<FirewallOutcome> processAsync(ServletRequest request, ServletResponse response) {
        return executor.submit(() -> doProcess(request, response));
    }

    /**
     * Process the request immediately
     *
     * @param request    the request to be processed by all {@link Processor}
     * @return the {@link FirewallOutcome} for this request
     * @see #doProcess(ServletRequest, ServletResponse)
     */
    public FirewallOutcome process(ServletRequest request) {
        return doProcess(request, null);
    }

    /**
     * Process the response immediately
     *
     * @param request     the request, for reference should the processor need it. Should not be changed.
     * @param response    the response, for analysis. Can be changed/rewritten by processors.
     * @return the {@link FirewallOutcome} for this request
     * @see #doProcess(ServletRequest, ServletResponse)
     */
    public FirewallOutcome process(ServletRequest request, ServletResponse response) {
        return doProcess(request, response);
    }

    /**
     * Sends the request and response to all processors, collecting the answers and calculating a final
     * {@link FirewallOutcome}
     *
     * There are two phases where this method is called: request and response. During the request phase, the response is
     * null. The request can be changed by the processors during this phase. During the response phase, we expect
     * both the request and response to be provided, but the request cannot be changed. The response can be changed.
     *
     * Before passing the request/response to the processors, an UUID is generated for the request. This UUID is then
     * added as a request attribute, under the name {@link #HTTP_HEADER_REQUEST_ID}. The request/response is then
     * recorded via {@link Recorder}.
     *
     * Each processor gets its own job that is submitted to the {@link ManagedExecutorService} via a
     * {@link CompletionService}.
     *
     * At the current implementation, the processing is finished once the first {@link FirewallOutcome#REJECT} is seen
     * and pending processors are cancelled.
     *
     * @param request     the incoming request
     * @param response    the response, can be null if the request has not been fully dispatched yet
     * @return  the outcome for the firewall processing
     */
    private FirewallOutcome doProcess(ServletRequest request, ServletResponse response) {
        if (null == request) {
            log.cannotAcceptProcessingWithoutRequest();
            return null;
        }

        String requestId;
        if (null != request.getAttribute(HTTP_HEADER_REQUEST_ID)) {
            requestId = request.getAttribute(HTTP_HEADER_REQUEST_ID).toString();
        } else {
            requestId = UUID.randomUUID().toString();
            request.setAttribute(HTTP_HEADER_REQUEST_ID, requestId);
        }

        if (null == response) {
            log.recordingRequest(requestId);
            recorder.record(request);
        } else {
            log.recordingResponse(requestId);
            recorder.record(response);
        }

        CompletionService<FirewallOutcome> completionService = new ExecutorCompletionService<>(executor);
        List<Future<FirewallOutcome>> listOfFutureOutcomes = new ArrayList<>(processors.size());
        FirewallOutcome outcome = FirewallOutcome.ACCEPT; // if no processors reject the request, we accept it

        try {
            processors
                    .stream()
                    .forEach(processor -> listOfFutureOutcomes
                            .add(completionService.submit(
                                    () -> response == null ? processor.process(request) : processor.process(response))
                            )
                    );

            for (int i = 0 ; i < processors.size() ; i++) {
                try {
                    outcome = completionService.take().get();
                    if (FirewallOutcome.REJECT.equals(outcome)) {
                        break;
                    }
                } catch (InterruptedException | ExecutionException ignored) {}
            }
        } finally {
            listOfFutureOutcomes.stream().forEach(future -> future.cancel(true));
        }

        log.finalOutcomeForRequest(requestId, outcome.toString());
        return outcome;
    }
}
