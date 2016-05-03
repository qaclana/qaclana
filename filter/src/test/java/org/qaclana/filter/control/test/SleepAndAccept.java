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
package org.qaclana.filter.control.test;

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@Startup
public class SleepAndAccept implements Processor {
    @Inject
    ProcessorRegistry registry;

    @PostConstruct
    public void register() {
        registry.register(this);
    }

    @Override
    public FirewallOutcome process(ServletRequest request) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            return FirewallOutcome.ACCEPT;
        }
        return FirewallOutcome.ACCEPT;
    }

    @Override
    public FirewallOutcome process(ServletResponse response) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            return FirewallOutcome.ACCEPT;
        }
        return FirewallOutcome.ACCEPT;
    }
}
