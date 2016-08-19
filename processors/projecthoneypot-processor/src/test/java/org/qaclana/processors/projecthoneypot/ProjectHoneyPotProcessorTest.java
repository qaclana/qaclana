/*
 * Copyright 2016 Juraci Paixão Kröhling
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
package org.qaclana.processors.projecthoneypot;

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
import org.qaclana.settings.EnvironmentVarsProvider;
import org.qaclana.settings.SettingsProvider;
import org.qaclana.settings.SettingsValue;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class ProjectHoneyPotProcessorTest {

    @Inject
    Processor processor;

    @Inject
    RecordLookupServiceMock recordLookupService;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(ProjectHoneyPotProcessor.class)
                .addClass(RecordLookupService.class)
                .addClass(RecordLookupServiceMock.class)
                .addClass(IpFoundOnHoneyPotBlacklist.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(ProcessorRegistry.class)
                .addClass(Processor.class)
                .addClass(FirewallOutcome.class)
                .addClass(SettingsValue.class)
                .addClass(SettingsProvider.class)
                .addClass(EnvironmentVarsProvider.class)
                .addClass(org.qaclana.settings.MsgLogger_$logger.class)
                .addClass(org.qaclana.settings.MsgLogger.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void rejectsIpWithRecords() {
        String ip = "255.255.255.255"; // static list on the mock
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ip);

        FirewallOutcome outcome = processor.process(request);
        assertEquals(FirewallOutcome.REJECT, outcome);
    }

    @Test
    public void neutralOnIpv6() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("::1");

        FirewallOutcome outcome = processor.process(request);
        assertEquals(FirewallOutcome.NEUTRAL, outcome);
    }
}
