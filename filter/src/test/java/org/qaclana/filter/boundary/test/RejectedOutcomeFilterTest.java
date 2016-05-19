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
package org.qaclana.filter.boundary.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.*;
import org.qaclana.api.entity.event.NewClientSocketMessage;
import org.qaclana.filter.boundary.FirewallFilter;
import org.qaclana.filter.control.*;
import org.qaclana.filter.control.test.RejectAllProcessor;
import org.qaclana.filter.control.test.SleepAndAccept;
import org.qaclana.filter.control.test.SleepAndReject;
import org.qaclana.filter.entity.IncomingHttpRequest;
import org.qaclana.filter.entity.OutgoingHttpResponse;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@RunWith(Arquillian.class)
public class RejectedOutcomeFilterTest {
    @Inject
    Firewall firewall;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(FirewallFilter.class)
                .addClass(Firewall.class)
                .addClass(FirewallOutcome.class)
                .addClass(SystemStateContainer.class)
                .addClass(SystemStateBasedFirewall.class)
                .addClass(IncomingHttpRequest.class)
                .addClass(SleepAndReject.class)
                .addClass(SleepAndAccept.class)
                .addClass(RejectAllProcessor.class)
                .addClass(Processor.class)
                .addClass(Recorder.class)
                .addClass(FilterOverheadMeasurer.class)
                .addClass(OverheadMeasureReporter.class)
                .addClass(OutgoingHttpResponse.class)
                .addClass(ProcessorRegistry.class)
                .addClass(NewClientSocketMessage.class)
                .addClass(SystemState.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("META-INF/ejb-jar.xml", "ejb-jar.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void ensureRequestGetsRejected() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        FirewallOutcome outcome = firewall.process(request);
        assertEquals(FirewallOutcome.REJECT, outcome);
    }

    @Test
    public void ensureResponseGetsRejected() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(Firewall.HTTP_HEADER_REQUEST_ID)).thenReturn(UUID.randomUUID().toString());
        HttpServletResponse response = mock(HttpServletResponse.class);
        FirewallOutcome outcome = firewall.process(request, response);
        assertEquals(FirewallOutcome.REJECT, outcome);
    }
}
