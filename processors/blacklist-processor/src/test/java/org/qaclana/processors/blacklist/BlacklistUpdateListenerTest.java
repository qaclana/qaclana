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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.BasicEvent;
import org.qaclana.api.entity.event.NewSocketMessage;
import org.qaclana.api.entity.ws.IpRangeAddedToBlacklistMessage;

import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@RunWith(Arquillian.class)
public class BlacklistUpdateListenerTest {
    private static CountDownLatch latch = new CountDownLatch(1);

    @Inject
    BlacklistContainer blacklistContainer;

    @Inject
    Event<NewSocketMessage> newClientSocketMessageEvent;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(BlacklistContainer.class)
                .addClass(BlacklistUpdateListener.class)
                .addClass(BlacklistUpdated.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(BasicEvent.class)
                .addClass(NewSocketMessage.class)
                .addClass(IpRange.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void ensureNewMessageWithIpAddsToBlacklist() throws InterruptedException {
        assertEquals(0, blacklistContainer.getBlacklistedIpRanges().size());

        IpRange blocked = IpRange.fromString("192.168.0.1/24");
        String template = "{\"ipRange\":{\"start\":%s,\"end\":%s},\"type\":\"blocked-iprange\"}";
        String payload = String.format(template, blocked.getStart(), blocked.getEnd());
        newClientSocketMessageEvent.fire(new NewSocketMessage(IpRangeAddedToBlacklistMessage.EVENT_TYPE, payload));
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(1, blacklistContainer.getBlacklistedIpRanges().size());
    }


    public void observesEvent(@Observes BlacklistUpdated blacklistUpdated) {
        latch.countDown();
    }
}
