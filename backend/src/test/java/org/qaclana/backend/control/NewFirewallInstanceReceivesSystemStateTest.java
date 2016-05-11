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
package org.qaclana.backend.control;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.SystemState;
import org.qaclana.backend.boundary.FirewallSocket;
import org.qaclana.backend.boundary.SystemStateEndpoint;
import org.qaclana.backend.entity.event.NewFirewallInstanceRegistered;
import org.qaclana.backend.entity.event.SendMessage;
import org.qaclana.backend.entity.event.SystemStateChange;
import org.qaclana.backend.entity.rest.SystemStateRequest;
import org.qaclana.backend.entity.ws.BasicMessage;
import org.qaclana.backend.entity.ws.SystemStateChangeMessage;

import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.websocket.Session;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@RunWith(Arquillian.class)
public class NewFirewallInstanceReceivesSystemStateTest {
    // static because the instance of our test is not the same as the singleton EJB
    private static CountDownLatch latch = new CountDownLatch(1); // we expect one message to arrive
    private static SendMessage receivedMessage;

    @Inject
    Event<NewFirewallInstanceRegistered> newFirewallInstanceRegisteredEvent;

    @Inject
    FirewallSocket firewallSocket;

    @Inject
    RunAsAdmin runAsAdmin;

    @Inject @Firewall
    private Instance<Map<String, Session>> sessionsInstance;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(FirewallSocket.class)
                .addClass(SystemStateEndpoint.class)
                .addPackage(SystemStateRequest.class.getPackage())
                .addPackage(SystemStateChangePropagator.class.getPackage())
                .addPackage(SystemStateChange.class.getPackage())
                .addPackage(SystemState.class.getPackage())
                .addPackage(BasicMessage.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void newInstanceIsRegisteredTest() throws Exception {
        // TODO: change this test to use actual web sockets once WFLY-3313 is fixed.
        runAsAdmin.call(() -> {
            assertEquals(0, sessionsInstance.get().size());
            Session session = mock(Session.class);
            firewallSocket.onOpen(session);

            latch.await(2, TimeUnit.SECONDS);

            assertEquals(1, sessionsInstance.get().size());
            assertTrue(receivedMessage.getMessage() instanceof SystemStateChangeMessage);
            SystemStateChangeMessage message = (SystemStateChangeMessage) receivedMessage.getMessage();
            assertNotNull(message.getState());
            return null;
        });
    }

    public void getMessage(@Observes SendMessage event) {
        receivedMessage = event;
        latch.countDown();
    }
}
