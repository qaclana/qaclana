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
package org.qaclana.processors.sqlinjection;

import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Analyzes a request for SQL injection, matching all the request parameter values against a list of regular expressions
 *
 * @author Juraci Paixão Kröhling
 */
@Singleton
@Startup
public class SqlInjectionProcessor implements Processor {
    private static final int MATCHING_MODE = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
    private static final List<Pattern> patterns = new ArrayList<>(5);

    static {
        // for my own future sanity:
        // .*'.* -> this will match anything plus single quotes plus anything
        // ".*([\r\n]+)?" -> this will match anything else, including possible newlines

        // ' DECLARE @var AS type - T-SQL. We are conservative here, by requiring another word for the type, so,
        // "' DECLARE @var AS " won't match but "' DECLARE @var AS type" will
        patterns.add(Pattern.compile(".*'.*\\bDECLARE\\b[\\s]+@\\w?.*([\\r\\n]+)?", MATCHING_MODE));

        // ' DELETE FROM
        patterns.add(Pattern.compile(".*'.*\\bDELETE\\b[\\s]+\\bFROM\\b.*([\\r\\n]+)?", MATCHING_MODE));

        // ' INSERT INTO
        patterns.add(Pattern.compile(".*'.*\\bINSERT\\b[\\s]+\\bINTO\\b.*([\\r\\n]+)?", MATCHING_MODE));

        // ' SELECT ... FROM
        patterns.add(Pattern.compile(".*'.*\\bSELECT\\b.*\\bFROM\\b.*([\\r\\n]+)?", MATCHING_MODE));

        // ' UPDATE SET
        patterns.add(Pattern.compile(".*'.*\\bUPDATE\\b.*\\bSET\\b.*([\\r\\n]+)?", MATCHING_MODE));

        // 1 AND/OR B BETWEEN C AND D
        patterns.add(Pattern.compile("^([\\d]+)?\\s\\b((AND)|(OR)).*\\bBETWEEN\\b.*AND.*$", MATCHING_MODE));
    }

    @Inject
    ProcessorRegistry processorRegistry;

    @Inject
    AttackAttemptReporter attackAttemptReporter;

    @PostConstruct
    public void register() {
        processorRegistry.register(this);
    }

    @Override
    public FirewallOutcome process(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String key : parameterMap.keySet()) {
            for (String value : parameterMap.get(key)) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(value).matches()) {
                        String requestId = request.getAttribute("Firewall-RequestID").toString();
                        request.setAttribute("Qaclana-Processor-SQLInjection", String.format("Pattern violated [%s]", pattern));
                        attackAttemptReporter.report(requestId, request.getRemoteAddr(), key, value, pattern);
                        return FirewallOutcome.REJECT;
                    }
                }

            }
        }
        return FirewallOutcome.NEUTRAL;
    }

    @Override
    public FirewallOutcome process(HttpServletResponse response) {
        return FirewallOutcome.NEUTRAL;
    }
}
