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
package org.qaclana.addons.blacklistupdater;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.addons.blacklistupdater.entity.JobFinished;
import org.qaclana.api.control.BlacklistService;
import org.qaclana.api.entity.IpRange;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class EmergingThreatsBatchTest {
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Inject
    BlacklistService blacklistService;

    @Inject
    EmergingThreatsJobStarter emergingThreatsJobStarter;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(IpRangeToBlacklistWriter.class.getPackage())
                .addPackage(BlacklistService.class.getPackage())
                .addPackage(IpRange.class.getPackage())
                .addPackage(JobFinished.class.getPackage())
                .addClass(BlacklistServiceTestImpl.class)
                .addClass(EmergingThreatsInputStreamAlternativeProvider.class)
                .addAsWebInfResource("beans.xml")
                .addAsResource("META-INF/batch-jobs/emerging-threats-updater.xml")
                .addAsResource("emerging-Block-IPs.txt")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void checkBatchRuns() throws InterruptedException {
        JobExecution jobExecution = emergingThreatsJobStarter.start();

        // we don't know anythign about this IP yet
        assertFalse(blacklistService.isInBlacklist(IpRange.fromString("1.178.179.217")));

        // we wait at most 5 seconds for the job to complete
        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(BatchStatus.COMPLETED, jobExecution.getBatchStatus());

        assertFalse(blacklistService.isInBlacklist(IpRange.fromString("127.0.0.1")));
        assertTrue(blacklistService.isInBlacklist(IpRange.fromString("1.178.179.217")));
    }

    public void observes(@Observes JobFinished ignored) {
        countDownLatch.countDown();
    }
}
