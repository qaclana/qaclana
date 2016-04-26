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

import org.qaclana.filter.entity.FirewallOutcome;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

        // if we have *one* processor that rejects this response, we cancel/interrupt all the future tasks and
        // accept "REJECT" as outcome
        AtomicBoolean rejected = new AtomicBoolean(false);

        // we use a latch to signal that we finished running all processors
        CountDownLatch latch = new CountDownLatch(processors.size());

        // we use a condition to signal that a reject outcome has reached *or* the latch reached 0, meaning, we finished
        // processing everything
        CountDownLatch rejectReachedOrProcessingFinished = new CountDownLatch(1);

        // we'll run all processors asynchronously. if any of them renders a REJECT as outcome, we signal our condition
        // no matter the outcome, we decrease the latch
        log.startQueueingProcessors(processors.size());
        for (Processor processor : processors) {
            CompletableFuture
                    .supplyAsync(
                            () -> {
                                log.startProcessor(processor.getClass().getName(), requestId);
                                return response == null ? processor.process(request) : processor.process(response);
                            }
                    )
                    .thenAccept((firewallOutcome -> {
                        log.finishedProcessor(processor.getClass().getName(), requestId, firewallOutcome.toString());
                        if (FirewallOutcome.REJECT.equals(firewallOutcome)) {
                            rejected.set(true);
                            rejectReachedOrProcessingFinished.countDown();
                        }
                        latch.countDown();
                    }));
        }

        // here, we wait wait for the processing to finish and, once it happens, we signal to the condition
        CompletableFuture.runAsync(() -> {
            try {
                log.waitingForProcessors(requestId);
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.waitInterruptedForProcessors(requestId, e);
            }
            log.finishedWaitingForProcessors(requestId);
            rejectReachedOrProcessingFinished.countDown();
        });

        // we wait for the condition to be reached: either everything processed fined, or at least one processor reached
        // a REJECTED outcome
        try {
            log.waitingForOutcomeOfRequest(requestId);
            rejectReachedOrProcessingFinished.await(10, TimeUnit.SECONDS);
            log.finishedWaitingForOutcomeOfRequest(requestId);
        } catch (InterruptedException e) {
            log.waitInterruptedForOutcome(requestId, e);
        }

        FirewallOutcome outcome = rejected.get() ? FirewallOutcome.REJECT : FirewallOutcome.ACCEPT;
        log.finalOutcomeForRequest(requestId, outcome.toString());
        return outcome;
    }
}
