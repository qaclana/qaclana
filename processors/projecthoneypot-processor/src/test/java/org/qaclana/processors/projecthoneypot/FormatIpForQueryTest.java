/*
 * Copyright 2016 Juraci Paixão Kröhling
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
package org.qaclana.processors.projecthoneypot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Juraci Paixão Kröhling
 */
public class FormatIpForQueryTest {

    @Test
    public void ensureCorrectFormattingOfAddressForQuery() {
        RecordLookupService recordLookupService = new RecordLookupService();
        String ip = "213.109.53.16";
        String formatted = recordLookupService.formatIpForQuery(ip);
        assertEquals("16.53.109.213", formatted);
    }

    @Test(expected = IllegalStateException.class)
    public void failsOnBadIp() {
        RecordLookupService recordLookupService = new RecordLookupService();
        String ip = "213.109.53.";
        recordLookupService.formatIpForQuery(ip);
    }

    @Test
    public void noopOnNull() {
        RecordLookupService recordLookupService = new RecordLookupService();
        String formatted = recordLookupService.formatIpForQuery(null);
        assertNull(formatted);
    }

}
