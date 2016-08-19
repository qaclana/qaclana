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
package org.qaclana.processors.whitelist;

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
import org.qaclana.api.entity.ws.IpRangeAddedToWhitelistMessage;

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
public class WhitelistUpdateListenerTest {
    private static CountDownLatch latch = new CountDownLatch(1);

    @Inject
    WhitelistContainer whitelistContainer;

    @Inject
    Event<NewSocketMessage> newClientSocketMessageEvent;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(WhitelistContainer.class)
                .addClass(WhitelistUpdateListener.class)
                .addClass(WhitelistUpdated.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(BasicEvent.class)
                .addClass(NewSocketMessage.class)
                .addClass(IpRange.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void ensureNewMessageWithIpAddsToWhitelist() throws InterruptedException {
        assertEquals(0, whitelistContainer.getIpRangesOnWhitelist().size());

        IpRange whitelisted = IpRange.fromString("192.168.0.1/24");
        String template = "{\"ipRange\":{\"start\":%s,\"end\":%s},\"type\":\"blocked-iprange\"}";
        String payload = String.format(template, whitelisted.getStart(), whitelisted.getEnd());
        newClientSocketMessageEvent.fire(new NewSocketMessage(IpRangeAddedToWhitelistMessage.EVENT_TYPE, payload));
        latch.await(1, TimeUnit.SECONDS);

        assertEquals(1, whitelistContainer.getIpRangesOnWhitelist().size());
    }


    public void observesEvent(@Observes WhitelistUpdated ignored) {
        latch.countDown();
    }
}
