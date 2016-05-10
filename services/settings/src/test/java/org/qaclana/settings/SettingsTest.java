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
package org.qaclana.settings;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class SettingsTest {

    @Inject
    SettingsProvider settingsProvider;

    @Inject @EnvironmentVars
    Map<String, String> envVars;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(SettingsProvider.class.getPackage())
                .addClass(EnvironmentVarsTestProvider.class)
                .addAsWebInfResource("beans.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void nonExistingValue() {
        assertNull(settingsProvider.get("non.existing.value"));
    }

    @Test
    public void valueFromJndi() throws NamingException {
        InitialContext context = new InitialContext();
        context.bind("java:global/qaclana/foo", "bar");
        context.bind("java:global/qaclana/name.with.namespace.key", "value");
        settingsProvider.reload();
        assertEquals("bar", settingsProvider.get("foo"));
        assertEquals("value", settingsProvider.get("name.with.namespace.key"));

        context.unbind("java:global/qaclana/foo");
        context.unbind("java:global/qaclana/name.with.namespace.key");
        settingsProvider.reload();
        assertEquals("bar", settingsProvider.get("foo"));
        assertEquals("value", settingsProvider.get("name.with.namespace.key"));
    }

    @Test
    public void valueFromSystemProperties() throws NamingException {
        System.setProperty("qaclana.foo", "bar");
        System.setProperty("qaclana.name.with.namespace.key", "value");
        settingsProvider.reload();

        assertEquals("bar", settingsProvider.get("foo"));
        assertEquals("value", settingsProvider.get("name.with.namespace.key"));

        System.clearProperty("qaclana.foo");
        System.clearProperty("qaclana.name.with.namespace.key");
        settingsProvider.reload();

        assertEquals("bar", settingsProvider.get("foo"));
        assertEquals("value", settingsProvider.get("name.with.namespace.key"));
    }

    @Test
    public void valueFromEnvVars() throws NamingException {
        envVars.put("QACLANA_FOO", "bar");
        envVars.put("QACLANA_NAME_WITH_NAMESPACE_KEY", "value");

        settingsProvider.reload();

        assertEquals("bar", settingsProvider.get("foo"));
        assertEquals("value", settingsProvider.get("name.with.namespace.key"));
    }
}
