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
package org.qaclana.filter.boundary;

import org.qaclana.filter.control.MsgLogger;
import org.qaclana.filter.control.SystemStateBasedFirewall;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * A servlet filter that intercepts requests and responses.
 *
 * @author Juraci Paixão Kröhling
 */
@WebFilter(urlPatterns = "/*", filterName = "QaclanaFilter")
public class FirewallFilter implements Filter {
    MsgLogger log = MsgLogger.LOGGER;

    @Inject
    SystemStateBasedFirewall systemStateBasedFirewall;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.filterInitialized();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // what can I say... I love EJBs!
        // the main reason for performing the filter on an EJB is to have monitoring capabilities + an unified way
        // to handle interceptors
        systemStateBasedFirewall.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        log.filterDestroyed();
    }
}
