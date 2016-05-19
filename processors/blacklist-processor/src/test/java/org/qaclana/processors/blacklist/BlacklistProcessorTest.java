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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;
import org.qaclana.api.entity.IpRange;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@RunWith(Arquillian.class)
public class BlacklistProcessorTest {
    @Inject
    BlacklistContainer blacklistContainer;

    @Inject
    Processor blacklistProcessor;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(BlacklistContainer.class)
                .addClass(BlacklistProcessor.class)
                .addClass(BlacklistUpdated.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(IpRange.class)
                .addClass(ProcessorRegistry.class)
                .addClass(Processor.class)
                .addClass(FirewallOutcome.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void requestGetsBlocked() throws InterruptedException {
        assertOutcome("192.168.0.5", FirewallOutcome.REJECT);
    }

    @Test
    public void requestGetsNeutral() throws InterruptedException {
        assertOutcome("10.0.0.1", FirewallOutcome.NEUTRAL);
    }

    private void assertOutcome(String ip, FirewallOutcome expectedOutcome) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ip);

        assertEquals(0, blacklistContainer.getBlacklistedIpRanges().size());

        IpRange blocked = IpRange.fromString("192.168.0.1/24");
        blacklistContainer.add(blocked);

        FirewallOutcome outcome = blacklistProcessor.process(request);

        blacklistContainer.remove(blocked);

        assertEquals(expectedOutcome, outcome);
    }
}
