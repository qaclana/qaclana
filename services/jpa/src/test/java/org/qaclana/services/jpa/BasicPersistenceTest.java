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
package org.qaclana.services.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.control.AuditService;
import org.qaclana.api.control.BlacklistService;
import org.qaclana.api.control.WhitelistService;
import org.qaclana.api.entity.Audit;
import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.*;
import org.qaclana.services.jpa.control.*;
import org.qaclana.services.jpa.entity.*;

import javax.inject.Inject;
import java.io.File;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class BasicPersistenceTest {

    @Inject
    BlacklistService blacklistService;

    @Inject
    WhitelistService whitelistService;

    @Inject
    AuditService auditService;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(AuditService.class)
                .addClass(AuditServiceJPA.class)
                .addClass(Audit.class)
                .addClass(AuditEntity.class)
                .addClass(AuditEntity_.class)
                .addClass(BlacklistService.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(BlacklistServiceJPA.class)
                .addClass(IpRange.class)
                .addClass(IpRangeEntity.class)
                .addClass(IpRangeEntity_.class)
                .addClass(IpRangeType.class)
                .addClass(JpaServiceResources.class)
                .addClass(BasicEvent.class)
                .addClass(IpRangeAddedToBlacklist.class)
                .addClass(IpRangeAddedToWhitelist.class)
                .addClass(QaclanaEntity.class)
                .addClass(QaclanaEntity_.class)
                .addClass(IpRangeRemovedFromBlacklist.class)
                .addClass(IpRangeRemovedFromWhitelist.class)
                .addClass(WhitelistService.class)
                .addClass(WhitelistServiceJPA.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void simplePersistenceTestForIpRange() throws Exception {
        whitelistService.add(IpRange.fromString("192.168.1.0/24"));
        blacklistService.add(IpRange.fromString("192.168.1.0/24"));

        assertTrue(whitelistService.isInWhitelist(IpRange.fromString("192.168.1.0/24")));
        assertTrue(blacklistService.isInBlacklist(IpRange.fromString("192.168.1.0/24")));
    }

    @Test
    public void storeAuditEvent() throws Exception {
        BigInteger remoteAddress = IpRange.fromString("127.0.0.1").getStart();
        Audit audit = new Audit(UUID.randomUUID().toString(), remoteAddress, "A random audit event.");
        auditService.add(audit);
        assertNotNull(auditService.get(audit.getId()));
    }
}
