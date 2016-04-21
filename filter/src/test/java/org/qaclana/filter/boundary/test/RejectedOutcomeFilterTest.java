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
import org.qaclana.api.SystemStateContainer;
import org.qaclana.filter.boundary.FirewallFilter;
import org.qaclana.filter.control.Firewall;
import org.qaclana.filter.control.test.RejectAllProcessor;
import org.qaclana.filter.entity.FirewallOutcome;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
                .addPackage(FirewallFilter.class.getPackage())
                .addPackage(Firewall.class.getPackage())
                .addPackage(FirewallOutcome.class.getPackage())
                .addPackage(SystemStateContainer.class.getPackage())
                .addClass(RejectAllProcessor.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void ensureRequestGetsRejected() {
        ServletRequest request = mock(ServletRequest.class);
        FirewallOutcome outcome = firewall.process(request);
        assertEquals(FirewallOutcome.REJECT, outcome);
    }

    @Test
    public void ensureResponseGetsRejected() {
        ServletRequest request = mock(ServletRequest.class);
        when(request.getAttribute("Firewall-RequestID")).thenReturn(UUID.randomUUID().toString());
        ServletResponse response = mock(ServletResponse.class);
        FirewallOutcome outcome = firewall.process(request, response);
        assertEquals(FirewallOutcome.REJECT, outcome);
    }
}
