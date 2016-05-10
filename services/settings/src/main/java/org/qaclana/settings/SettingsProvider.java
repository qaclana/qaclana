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

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
@Startup
public class SettingsProvider {
    private static final String JNDI_BASE = "java:global/qaclana";
    private static final String CONFIG_FILE_PATH = "/etc/qaclana.conf";
    MsgLogger logger = MsgLogger.LOGGER;

    private Map<String, String> properties = new HashMap<>();
    private Map<String, String> fromJNDI = new HashMap<>();
    private Properties fromConfigurationFile = new Properties();

    @Inject @EnvironmentVars
    Map<String, String> envVars;

    public String get(String settingsName) {
        // Resolution order:
        // * Local cache map (ie: it has been resolved already)
        // * System property (-Dqaclana.property.name=value)
        // * Environment var (QACLANA_PROPERTY_NAME)
        // * JNDI global (java:global/qaclana/property.name)
        // * /etc/qaclana.conf , with entries like from the system property: property.name=value

        if (properties.containsKey(settingsName)) {
            return properties.get(settingsName);
        }

        String fromSystemProperties = System.getProperty(getSettingsNameForSysprop(settingsName));
        if (null != fromSystemProperties) {
            properties.put(settingsName, fromSystemProperties);
            return fromSystemProperties;
        }

        String fromEnvVars = envVars.get(getSettingsNameForEnvVar(settingsName));
        if (null != fromEnvVars) {
            properties.put(settingsName, fromEnvVars);
            return fromEnvVars;
        }

        if (fromJNDI.containsKey(getSettingsNameForJndi(settingsName))) {
            String value = fromJNDI.get(settingsName);
            properties.put(settingsName, value);
            return value;
        }

        if (fromConfigurationFile.contains(settingsName)) {
            String value = (String) fromConfigurationFile.get(settingsName);
            properties.put(settingsName, value);
            return value;
        }

        return null;
    }

    @Produces @SettingsValue
    public String produceSettingsValue(InjectionPoint injectionPoint) {
        SettingsValue settingsValue = injectionPoint.getAnnotated().getAnnotation(SettingsValue.class);
        String settingsName = settingsValue.value();
        return get(settingsName);
    }

    @PostConstruct
    public void reload() {
        parseEntriesFromFile();
        parseEntriesFromJNDI();
    }

    private void parseEntriesFromJNDI() {
        logger.loadingSettingsFromJndi(JNDI_BASE);
        try {
            NamingEnumeration<Binding> settingsFromJndi = new InitialContext().listBindings(JNDI_BASE);
            while (settingsFromJndi.hasMore()) {
                Binding pair = settingsFromJndi.next();
                String name = pair.getName();
                String value = pair.getObject().toString();
                fromJNDI.put(name, value);
            }
        } catch (NamingException ignored) {
            // we just didn't find any value on JNDI
        }
    }

    private void parseEntriesFromFile() {
        logger.loadingSettingsFromFile(CONFIG_FILE_PATH);
        File file = new File(CONFIG_FILE_PATH);
        if (file.exists() && file.canRead()) {
            try {
                fromConfigurationFile = new Properties();
                fromConfigurationFile.load(new FileInputStream(file));
            } catch (IOException e) {
                // failed to read the file... we need to report, but not to panic
                logger.reportProblemInReadingConfig(e);
            }
        }
    }

    private String getSettingsNameForEnvVar(String settingsName) {
        if (null == settingsName || settingsName.isEmpty()) {
            return null;
        }
        return "QACLANA_" + settingsName.toUpperCase().replaceAll("\\.", "_");
    }

    private String getSettingsNameForSysprop(String settingsName) {
        if (null == settingsName || settingsName.isEmpty()) {
            return null;
        }
        return "qaclana." + settingsName;
    }

    private String getSettingsNameForJndi(String settingsName) {
        return settingsName;
    }

}
