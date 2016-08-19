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

import org.qaclana.addons.blacklistupdater.entity.JobFinished;
import org.qaclana.addons.blacklistupdater.entity.JobStarted;

import javax.batch.api.listener.JobListener;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This is a Batch Listener that converts events into CDI events, which can be caught by non-batch elements.
 *
 * @author Juraci Paixão Kröhling
 */
@Named
public class BatchToCdiListener implements JobListener {
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    Event<JobStarted> jobStartedEvent;

    @Inject
    Event<JobFinished> jobFinishedEvent;

    @Override
    public void beforeJob() throws Exception {
        logger.batchJobStarted();
        jobStartedEvent.fire(new JobStarted());
    }

    @Override
    public void afterJob() throws Exception {
        logger.batchJobFinished();
        jobFinishedEvent.fire(new JobFinished());
    }
}
