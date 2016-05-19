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

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.SystemStateContainer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static org.qaclana.filter.control.Firewall.HTTP_HEADER_REQUEST_ID;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class SystemStateBasedFirewall {
    @Inject
    SystemStateContainer systemStateContainer;

    @Inject
    Firewall firewall;

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse)) {
            // should never happen... but if it does, we are not interested on them anyway
            return;
        }

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

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

                if (!FirewallOutcome.REJECT.equals(outcome)) {
                    chain.doFilter(request, response);
                    outcome = firewall.process(request, response);
                    if (FirewallOutcome.REJECT.equals(outcome)) {
                        reject(request, response);
                        return;
                    }
                } else {
                    reject(request, response);
                    return;
                }
                return;
            default:
                throw new IllegalStateException("Unknown system state.");
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (name.toLowerCase().startsWith("qaclana-processor")) {
                response.addHeader(name, request.getAttribute(name).toString());
            }
        }
        response.addHeader("Qaclana-Request-ID", request.getAttribute(HTTP_HEADER_REQUEST_ID).toString());
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
