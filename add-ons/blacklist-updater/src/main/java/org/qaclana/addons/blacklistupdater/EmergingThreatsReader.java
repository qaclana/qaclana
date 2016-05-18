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
package org.qaclana.addons.blacklistupdater;

import org.qaclana.addons.blacklistupdater.entity.EmergingThreats;

import javax.batch.api.chunk.ItemReader;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 * Batch reader for Emerging Threats blacklist.
 *
 * @author Juraci Paixão Kröhling
 */
@Named
public class EmergingThreatsReader implements ItemReader {
    private BufferedReader in;

    @Inject @EmergingThreats
    private transient InputStream emergingThreatsInputStream;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        // we don't care much about the checkpoint... if we failed before, or if we are restarting, we just
        // get a fresh list from the provider
        InputStreamReader isr = new InputStreamReader(emergingThreatsInputStream);
        in = new BufferedReader(isr);
    }

    @Override
    public void close() throws Exception {
        if (in != null) {
            in.close();
        }
    }

    @Override
    public Object readItem() throws Exception {
        return in.readLine();
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
}
