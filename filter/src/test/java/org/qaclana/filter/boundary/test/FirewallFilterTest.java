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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.SystemState;
import org.qaclana.api.SystemStateContainer;
import org.qaclana.filter.boundary.FirewallFilter;
import org.qaclana.filter.control.Firewall;
import org.qaclana.filter.control.SocketClient;
import org.qaclana.filter.control.test.ApplicationResourcesForTest;
import org.qaclana.filter.control.test.ContextPathContainer;
import org.qaclana.filter.control.test.ServletContextStartupListener;
import org.qaclana.filter.control.test.SocketServer;
import org.qaclana.filter.entity.FirewallOutcome;
import org.qaclana.filter.entity.IncomingHttpRequest;
import org.qaclana.filter.entity.OutgoingHttpResponse;

import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.DeploymentException;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@RunWith(Arquillian.class)
public class FirewallFilterTest {
    private static CountDownLatch latch;
    private static IncomingHttpRequest requestEvent;
    private static OutgoingHttpResponse responseEvent;

    @Inject
    FirewallFilter filter;

    @Inject
    SystemStateContainer systemStateContainer;

    @Inject
    SocketClient socketClient;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(FirewallFilter.class.getPackage())
                .addPackage(Firewall.class.getPackage())
                .addPackage(FirewallOutcome.class.getPackage())
                .addPackage(SystemStateContainer.class.getPackage())
                .addClass(ContextPathContainer.class)
                .addClass(ServletContextStartupListener.class)
                .addClass(ApplicationResourcesForTest.class)
                .addClass(SocketServer.class)
                .addAsWebInfResource("beans.xml", "beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void enforcingShouldIssueRecorderEvents() throws Exception {
        latch = new CountDownLatch(2); // one response and one request
        requestEvent = null;
        responseEvent = null;

        systemStateContainer.setState(SystemState.ENFORCING);
        ServletResponse response = mock(ServletResponse.class);
        ServletRequest request = mock(ServletRequest.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);

        latch.await(500, TimeUnit.MILLISECONDS);
        assertNotNull(requestEvent);
        assertNotNull(responseEvent);
    }

    @Test
    public void permissiveShouldIssueRecorderEvents() throws Exception {
        latch = new CountDownLatch(2); // one response and one request
        requestEvent = null;
        responseEvent = null;

        systemStateContainer.setState(SystemState.PERMISSIVE);
        ServletResponse response = mock(ServletResponse.class);
        ServletRequest request = mock(ServletRequest.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);

        latch.await(500, TimeUnit.MILLISECONDS);
        assertNotNull(requestEvent);
        assertNotNull(responseEvent);
    }

    @Test
    public void everyRequestReceivesAnId() throws IOException, ServletException {
        latch = new CountDownLatch(2); // ignored on this test

        systemStateContainer.setState(SystemState.ENFORCING);
        ServletResponse response = mock(ServletResponse.class);
        ServletRequest request = mock(HttpServletRequest.class);
        FilterChain chain = mock(FilterChain.class);

        String idForSecondCall = UUID.randomUUID().toString();
        when(request.getAttribute(Firewall.HTTP_HEADER_REQUEST_ID))
                .thenReturn(null) // on the first call, no ID is there
                .thenReturn(idForSecondCall); // on the subsequent calls, there is an ID

        filter.doFilter(request, response, chain);

        verify(request, atLeast(2)).getAttribute(eq(Firewall.HTTP_HEADER_REQUEST_ID));
        verify(request).setAttribute(eq(Firewall.HTTP_HEADER_REQUEST_ID), any()); // we expect exactly one set call
    }

    @Test
    public void keepExistingId() throws IOException, ServletException, DeploymentException {
        latch = new CountDownLatch(2); // ignored on this test

        systemStateContainer.setState(SystemState.ENFORCING);
        ServletResponse response = mock(ServletResponse.class);
        ServletRequest request = mock(HttpServletRequest.class);
        FilterChain chain = mock(FilterChain.class);

        String requestId = UUID.randomUUID().toString();
        when(request.getAttribute(Firewall.HTTP_HEADER_REQUEST_ID)).thenReturn(requestId);

        filter.doFilter(request, response, chain);

        verify(request, atLeast(2)).getAttribute(eq(Firewall.HTTP_HEADER_REQUEST_ID));
        verify(request, never()).setAttribute(eq(Firewall.HTTP_HEADER_REQUEST_ID), any()); // we expect exactly one set call
    }

    public void getMessage(@Observes IncomingHttpRequest event) {
        requestEvent = event;
        latch.countDown();
    }

    public void getMessage(@Observes OutgoingHttpResponse event) {
        responseEvent = event;
        latch.countDown();
    }
}
