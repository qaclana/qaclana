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
package org.qaclana.processors.blocklist;

import org.qaclana.api.entity.IpRange;
import org.qaclana.api.entity.event.NewClientSocketMessage;
import org.qaclana.api.entity.ws.BlockedIpRangeMessage;
import org.qaclana.api.entity.ws.UnblockedIpRangeMessage;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.math.BigInteger;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
@Asynchronous
public class BlocklistUpdateListener {
    MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    BlocklistContainer blocklistContainer;

    public void blockedIpRange(@Observes NewClientSocketMessage message) {
        if (!BlockedIpRangeMessage.EVENT_TYPE.equals(message.getType())) {
            return;
        }

        IpRange ipRange = getIpRangeFromMessage(message.getMessage());
        blocklistContainer.add(ipRange);
        logger.addedIpRangeToBlocklist(ipRange.toString());
    }

    public void unblockedIpRange(@Observes NewClientSocketMessage message) {
        if (!UnblockedIpRangeMessage.EVENT_TYPE.equals(message.getType())) {
            return;
        }

        IpRange ipRange = getIpRangeFromMessage(message.getMessage());
        blocklistContainer.remove(ipRange);
        logger.removedIpRangeFromBlocklist(ipRange.toString());
    }

    private IpRange getIpRangeFromMessage(String message) {
        JsonReader reader = Json.createReader(new StringReader(message));
        JsonObject object = reader.readObject();
        JsonObject ipRangeObject = object.getJsonObject("ipRange");

        if (!ipRangeObject.containsKey("start") || !ipRangeObject.containsKey("end")) {
            logger.invalidMessage(message);
            throw new IllegalStateException("Unable to determine the IP Range from the socket message.");
        }

        BigInteger start = ipRangeObject.getJsonNumber("start").bigIntegerValue();
        BigInteger end = ipRangeObject.getJsonNumber("end").bigIntegerValue();
        return new IpRange(start, end);
    }

}
