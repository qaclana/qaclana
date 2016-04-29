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

import org.qaclana.api.SystemStateContainer;
import org.qaclana.filter.entity.FirewallOutcome;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class SystemStateBasedFirewall {
    @Inject
    SystemStateContainer systemStateContainer;

    @Inject
    Firewall firewall;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        switch (systemStateContainer.getState()) {
            case DISABLED:
                chain.doFilter(request, response);
                return;
            case PERMISSIVE:
                firewall.processAsync(request);
                chain.doFilter(request, response);
                firewall.processAsync(request, response);
                return;
            case ENFORCING:
                FirewallOutcome outcome;
                outcome = firewall.process(request);

                if (FirewallOutcome.ACCEPT.equals(outcome)) {
                    chain.doFilter(request, response);
                    outcome = firewall.process(request, response);
                    if (FirewallOutcome.REJECT.equals(outcome)) {
                        reject(response);
                        return;
                    }
                } else {
                    reject(response);
                    return;
                }
                return;
            default:
                throw new IllegalStateException("Unknown system state.");
        }
    }

    private void reject(ServletResponse response) throws IOException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            // we don't know how to tell the client that this request is blocked, but we do know that
            // we want to reset the response
            response.reset();
        }
    }
}
