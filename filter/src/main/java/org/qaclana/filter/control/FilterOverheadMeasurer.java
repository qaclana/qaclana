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
package org.qaclana.filter.control;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * EJB interceptor that wraps target calls and measures the time it took to run, reporting this data to a
 * {@link OverheadMeasureReporter}
 *
 * @author Juraci Paixão Kröhling
 */
public class FilterOverheadMeasurer {

    @Inject
    OverheadMeasureReporter reporter;

    @AroundInvoke
    public Object measure(InvocationContext invocationContext) throws Exception {
        long begin = System.currentTimeMillis();
        try {
            return invocationContext.proceed();
        } finally {
            long end = System.currentTimeMillis();
            reporter.report(invocationContext.getTarget().getClass(), invocationContext.getMethod(), begin, end);
        }
    }
}
