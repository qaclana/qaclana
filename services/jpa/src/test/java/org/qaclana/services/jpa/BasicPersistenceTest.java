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
package org.qaclana.services.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.control.BlocklistService;
import org.qaclana.api.control.WhitelistService;
import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.NewBlockedIpRange;
import org.qaclana.api.entity.event.RemovedBlockedIpRange;
import org.qaclana.services.jpa.control.BlocklistServiceJPA;
import org.qaclana.services.jpa.control.JpaServiceResources;
import org.qaclana.services.jpa.control.WhitelistServiceJPA;
import org.qaclana.services.jpa.entity.*;

import javax.inject.Inject;
import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class BasicPersistenceTest {

    @Inject
    BlocklistService blocklistService;

    @Inject
    WhitelistService whitelistService;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(BlocklistService.class)
                .addClass(BlocklistServiceJPA.class)
                .addClass(IpRange.class)
                .addClass(IpRangeEntity.class)
                .addClass(IpRangeEntity_.class)
                .addClass(IpRangeType.class)
                .addClass(JpaServiceResources.class)
                .addClass(NewBlockedIpRange.class)
                .addClass(QaclanaEntity.class)
                .addClass(QaclanaEntity_.class)
                .addClass(RemovedBlockedIpRange.class)
                .addClass(WhitelistService.class)
                .addClass(WhitelistServiceJPA.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void simplePersistenceTestForIpRange() throws Exception {
        whitelistService.add(IpRange.fromString("192.168.1.0/24"));
        blocklistService.add(IpRange.fromString("192.168.1.0/24"));

        assertTrue(whitelistService.isInWhitelist(IpRange.fromString("192.168.1.0/24")));
        assertTrue(blocklistService.isInBlocklist(IpRange.fromString("192.168.1.0/24")));
    }
}
