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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.SystemState;
import org.qaclana.api.SystemStateContainer;
import org.qaclana.api.entity.ws.BasicMessage;
import org.qaclana.api.entity.ws.SystemStateChangeMessage;
import org.qaclana.backend.boundary.FirewallSocket;
import org.qaclana.backend.boundary.SystemStateEndpoint;
import org.qaclana.backend.entity.event.BasicEvent;
import org.qaclana.backend.entity.event.NewFirewallInstanceRegistered;
import org.qaclana.backend.entity.event.SendMessage;
import org.qaclana.backend.entity.event.SystemStateChange;
import org.qaclana.backend.entity.rest.ErrorResponse;
import org.qaclana.backend.entity.rest.SystemStateRequest;

import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@RunWith(Arquillian.class)
public class SystemStateEndpointTest {
    // static because the instance of our test is not the same as the singleton EJB
    private static CountDownLatch latch; // we expect one message to arrive
    private static SystemStateChange changeEvent;

    @Inject
    SystemStateEndpoint endpoint;

    @Inject
    RunAsAdmin runAsAdmin;

    @Inject
    SystemStateContainer systemStateInstance;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(BasicEvent.class)
                .addClass(BasicMessage.class)
                .addClass(ErrorResponse.class)
                .addClass(FirewallSocket.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(NewFirewallInstanceRegistered.class)
                .addClass(RunAsAdmin.class)
                .addClass(RunAsAdmin.class)
                .addClass(SendMessage.class)
                .addClass(SystemState.class)
                .addClass(SystemStateChange.class)
                .addClass(SystemStateChangePropagator.class)
                .addClass(SystemStateChangeMessage.class)
                .addClass(SystemStateContainer.class)
                .addClass(SystemStateEndpoint.class)
                .addClass(SystemStateRequest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void changeOfSystemStateIsPropagatedTest() throws Exception {
        // TODO: change this test to use actual web sockets once WFLY-3313 is fixed.
        runAsAdmin.call(() -> {
            latch = new CountDownLatch(1);
            changeEvent = null;

            // ensure we have a sane start
            assertEquals(SystemState.DISABLED, systemStateInstance.getState());

            // we then change the system state
            SystemStateRequest request = new SystemStateRequest();
            request.setState(SystemState.ENFORCING.name());
            endpoint.update(request);

            latch.await(500, TimeUnit.MILLISECONDS);

            assertEquals(SystemState.ENFORCING, changeEvent.getState());
            assertEquals(SystemState.ENFORCING, systemStateInstance.getState());

            // now, we revert the system state
            request.setState(SystemState.DISABLED.name());
            endpoint.update(request);
            return null;
        });
    }

    @Test
    public void invalidSystemStateDoesNotChangeSystemState() throws Exception {
        // TODO: change this test to use actual web sockets once WFLY-3313 is fixed.
        runAsAdmin.call(() -> {
            latch = new CountDownLatch(1);
            changeEvent = null;

            // ensure we have a sane start
            assertEquals(SystemState.DISABLED, systemStateInstance.getState());

            // we then change the system state
            SystemStateRequest request = new SystemStateRequest();
            request.setState("INVALID");
            Response response = endpoint.update(request);

            // this should *always* timeout, as we never expect to get an event
            latch.await(500, TimeUnit.MILLISECONDS);

            assertNull(changeEvent);
            assertTrue(response.getEntity() instanceof ErrorResponse);
            ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
            assertEquals("invalid_system_state", errorResponse.getCode());

            return null;
        });
    }

    public void getMessage(@Observes SystemStateChange event) {
        changeEvent = event;
        latch.countDown();
    }
}
