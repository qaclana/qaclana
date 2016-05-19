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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.SystemState;
import org.qaclana.api.entity.event.SystemStateChange;
import org.qaclana.api.entity.ws.BasicMessage;
import org.qaclana.backend.boundary.FirewallSocket;
import org.qaclana.backend.entity.rest.SystemStateRequest;

import javax.ejb.Stateless;
import javax.websocket.*;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.junit.Assert.assertEquals;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
@Stateless
@ClientEndpoint
public class NewFirewallInstanceTriggersEventTest {
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(FirewallSocket.class.getPackage())
                .addPackage(SystemStateRequest.class.getPackage())
                .addPackage(SystemStateChangePropagator.class.getPackage())
                .addPackage(SystemStateChange.class.getPackage())
                .addPackage(SystemState.class.getPackage())
                .addPackage(BasicMessage.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test @Ignore("This test is ignored because of a possible bug in Wildfly: WFLY-3313")
    public void makeNewConnection() throws Exception {
        String url = "ws://"+baseURL.getHost()+":"+baseURL.getPort()+baseURL.getPath()+"ws/instance";

        final AtomicReference<String> message = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Endpoint endpoint = new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                session.addMessageHandler(new MessageHandler.Whole<String>() {
                    @Override
                    public void onMessage(String content) {
                        message.set(content);
                        latch.countDown();
                    }
                });
            }
        };

        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Collections.singletonList("Basic " + printBase64Binary("admin:admin".getBytes())));
            }
        };
        ClientEndpointConfig authorizationConfiguration = ClientEndpointConfig.Builder.create()
                .configurator(configurator)
                .build();

        Session session = ContainerProvider.getWebSocketContainer()
                .connectToServer(
                        endpoint, authorizationConfiguration,
                        new URI(url));

        latch.await(10, TimeUnit.SECONDS);
        session.close();

        assertEquals("", message.get());
    }
}
