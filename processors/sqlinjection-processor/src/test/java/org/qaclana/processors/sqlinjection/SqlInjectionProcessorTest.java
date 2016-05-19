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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.FirewallOutcome;
import org.qaclana.api.Processor;
import org.qaclana.api.ProcessorRegistry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class SqlInjectionProcessorTest {

    @Inject
    Processor sqlInjectionProcessor;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(SqlInjectionProcessor.class)
                .addClass(MsgLogger.class)
                .addClass(MsgLogger_$logger.class)
                .addClass(ProcessorRegistry.class)
                .addClass(Processor.class)
                .addClass(FirewallOutcome.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void catchesDeclareForTSql() {
        assertRequestIsRejectedForParameterValue("' declare @var1 varchar(10), @var2 varchar(255)");
//        assertRequestIsRejectedForParameterValue("' declare @var1 varchar(10), @var2 varchar(255)\n");
//        assertRequestIsRejectedForParameterValue("' declare @var1 varchar(10), @var2\\nvarchar(255)");
//        assertRequestIsRejectedForParameterValue("' declare @var1 varchar(10), @var2\\n/*some comment*/ varchar(255)");
    }

    @Test
    public void catchesNumericWithExtraCommands() {
        assertRequestIsRejectedForParameterValue("1 AND A NOT BETWEEN 0 AND B--");
//        assertRequestIsRejectedForParameterValue("1 AND A NOT BETWEEN 0 AND B--\n");
    }

    @Test
    public void catchesDeleteStatement() {
        assertRequestIsRejectedForParameterValue("' or 1=1 ; delete from table");
    }

    @Test
    public void catchesInsertStatement() {
        assertRequestIsRejectedForParameterValue("';insert into table () values () ");
    }

    @Test
    public void catchesSelectStatement() {
        assertRequestIsRejectedForParameterValue("';select * from admins");
    }

    @Test
    public void catchesUpdateStatement() {
        assertRequestIsRejectedForParameterValue("  aa '  ;      update   users   set role='admin' where login = 'jdoe'");
    }

    @Test
    public void falsePositives() {
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("param", new String[]{"update users on the company's strategy"});
        parameterMap.put("param2", new String[]{"O'Brian likes to select the best fruits on the market"});
        parameterMap.put("param3", new String[]{"O'Connor declare that @ his company there's ... "});
        assertRequestIsNeutralForMap(parameterMap);
    }

    private void assertRequestIsRejectedForParameterValue(String value) {
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("param", new String[]{value});
        assertRequestIsRejectedForMap(parameterMap);
    }

    private void assertRequestIsRejectedForMap(Map<String, String[]> parameterMap) {
        assertRequestForMap(parameterMap, FirewallOutcome.REJECT);
    }

    private void assertRequestIsNeutralForMap(Map<String, String[]> parameterMap) {
        assertRequestForMap(parameterMap, FirewallOutcome.NEUTRAL);
    }

    private void assertRequestForMap(Map<String, String[]> parameterMap, FirewallOutcome expectedOutcome) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(parameterMap);

        FirewallOutcome outcome = sqlInjectionProcessor.process(request);
        assertEquals(expectedOutcome, outcome);
    }
}
