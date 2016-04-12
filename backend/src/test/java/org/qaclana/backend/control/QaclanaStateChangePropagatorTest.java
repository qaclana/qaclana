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
import org.qaclana.backend.entity.event.SystemStateChange;
import org.qaclana.backend.entity.ws.BasicMessage;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * This is just a simple test, to get the Arquillian integration right. It has no business purpose.
 *
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class QaclanaStateChangePropagatorTest {

    @Inject
    Event<SystemStateChange> systemStateChangeEvent;

    @Inject
    RunAsAdmin runAsAdmin;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(SystemStateChangePropagator.class.getPackage())
                .addPackage(SystemStateChange.class.getPackage())
                .addPackage(SystemState.class.getPackage())
                .addPackage(BasicMessage.class.getPackage())
                .addClass(RunAsAdmin.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testFireEvent() throws Exception {
        runAsAdmin.call(() -> {
            systemStateChangeEvent.fire(new SystemStateChange(SystemState.DISABLED));
            return null;
        });
    }
}
