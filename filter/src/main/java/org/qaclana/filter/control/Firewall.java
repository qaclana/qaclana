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
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class Firewall {
    public static final String HTTP_HEADER_REQUEST_ID = "Firewall-RequestID";

    MsgLogger log = MsgLogger.LOGGER;

    @Inject
    Recorder recorder;

    @Inject
    List<Processor> processors;

    @Resource
    private ManagedExecutorService executor;

    public Future<FirewallOutcome> processAsync(ServletRequest request) {
        return executor.submit(() -> doProcess(request, null));
    }

    public Future<FirewallOutcome> processAsync(ServletRequest request, ServletResponse response) {
        return executor.submit(() -> doProcess(request, response));
    }

    public FirewallOutcome process(ServletRequest request) {
        return doProcess(request, null);
    }

    public FirewallOutcome process(ServletRequest request, ServletResponse response) {
        return doProcess(request, response);
    }

    /**
     * Either request or response has to be provided. Providing both is an error. One of them has to be null.
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
