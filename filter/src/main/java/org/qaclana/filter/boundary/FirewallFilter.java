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
import org.qaclana.filter.control.Recorder;
import org.qaclana.filter.control.SocketClient;
import org.qaclana.filter.control.SystemStateContainer;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * A servlet filter that intercepts requests and responses.
 *
 * @author Juraci Paixão Kröhling
 */
@WebFilter
public class FirewallFilter implements Filter {
    MsgLogger log = MsgLogger.LOGGER;

    @Inject
    SocketClient socketClient;

    @Inject
    SystemStateContainer systemStateContainer;

    @Inject
    Recorder recorder;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        switch (systemStateContainer.getState()) {
            case DISABLED:
                return;
            case PERMISSIVE:
                recorder.record(request);
                chain.doFilter(request, response);
                recorder.record(response);
                return;
            case ENFORCING:
                // TODO: we have to wait for an answer, for now, return true as well
                return;
            default:
                //noinspection UnnecessaryReturnStatement
                return;
        }
    }

    @Override
    public void destroy() {

    }
}
