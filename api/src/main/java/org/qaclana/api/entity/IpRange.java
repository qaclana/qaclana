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
package org.qaclana.api.entity;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Stores an IP range in a format that is compatible to both IPv4 and IPv6.
 *
 * @author Juraci Paixão Kröhling
 */
public class IpRange {

    /**
     * Stores the first IP of this range, inclusive. Note that we don't use {@link InetAddress} to store it because
     * of overflows on IPv6 addresses.
     */
    private BigInteger start;

    /**
     * Stores the last IP of this range, inclusive.
     */
    private BigInteger end;

    /**
     * Constructs a new IP range, based on a starting and ending IP expressed as longs.
     *
     * @param startIp the start IP boundary, as long. Required.
     * @param endIp   the upper IP boundary, as long. Required.
     */
    public IpRange(BigInteger startIp, BigInteger endIp) {
        if (null == startIp || null == endIp) {
            throw new IllegalArgumentException("The start IP and end IP have to be provided.");
        }

        if (endIp.compareTo(startIp) < 0) {
            throw new IllegalArgumentException("The end IP should be higher than the start IP.");
        }

        this.start = startIp;
        this.end = endIp;
    }

    /**
     * Creates a new IP Range based on a CIDR string. Formats understood by this method:
     * IPv4/Bits, ie, 192.168.0.1/32
     * IPv4/32, ie, 192.168.0.1 (becomes 192.168.0.1/32)
     * IPv6/Bits, ie, ::1
     * IPv6/128, ie, ::1 (becomes ::1/128)
     *
     * @param cidr the CIDR to be parsed
     * @return a new IpRange instance based on the CIDR data
     */
    public static IpRange fromString(String cidr) {
        // implementation note: if we were dealing with ipv4 only, we could store everything in longs,
        // but I think it's *wrong* to simply ignore ipv6. We will end up consuming more memory as we potentially would,
        // as most of the connections that we'll see are still ipv4, but we should do the right thing unless we have
        // a very, very, strong reason not to.

        String network;
        short maxRoutingPrefix = 32; // valid for ipv4, we'll make it 128 if we see that it's ipv6
        short routingPrefix = 32; // valid for ipv4, we'll make it 128 if we see that it's ipv6
        boolean singleIp = false; // are we dealing with a range, or a single IP?
        if (cidr.indexOf('/') > 0) {
            String[] parts = cidr.split("/");
            network = parts[0];
            routingPrefix = new Short(parts[1]);
        } else {
            // no explicit mask, it's a 'single' IP, so, start == end == ip
            network = cidr;
            singleIp = true;
        }

        try {
            InetAddress ipBase = InetAddress.getByName(network);
            if (ipBase instanceof Inet6Address) {
                maxRoutingPrefix <<= 2; // max for ipv4 is 32, but for ipv6 it's 128, so, we multiply 32 by 4 (or shift 2)
            }
            if (singleIp) {
                // we have a single IP, so, the mask is the maxRoutingPrefix
                // this is for the following cases:
                // 192.168.0.1 -> 192.168.0.1/32
                // FE80:: -> FE80::/128
                routingPrefix = maxRoutingPrefix;
            }

            if (routingPrefix > maxRoutingPrefix || routingPrefix <= 0) {
                throw new IllegalArgumentException("Invalid routing prefix (/" + routingPrefix + ") on CIDR " + cidr);
            }

            BigInteger ipAsNumeric = ipToBigInteger(ipBase);
            BigInteger mask = BigInteger.valueOf(-1).shiftLeft(maxRoutingPrefix - routingPrefix);
            BigInteger startingAddress = ipAsNumeric.and(mask);
            BigInteger endAddress = startingAddress.add(mask.not());
            return new IpRange(startingAddress, endAddress);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("The given CIDR is invalid: " + cidr, e);
        }
    }

    public static IpRange fromJson(String json) {
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonObject object = reader.readObject();
        JsonObject ipRangeObject = object.getJsonObject("ipRange");

        if (!ipRangeObject.containsKey("start") || !ipRangeObject.containsKey("end")) {
            throw new IllegalStateException("Unable to determine the IP Range from the socket message.");
        }

        BigInteger start = ipRangeObject.getJsonNumber("start").bigIntegerValue();
        BigInteger end = ipRangeObject.getJsonNumber("end").bigIntegerValue();
        return new IpRange(start, end);
    }

    public static BigInteger ipToBigInteger(InetAddress ipBase) {
        BigInteger ipAsNumeric = BigInteger.ZERO;

        // we use a loop, as we might have either an IPv4 or an IPv6 address
        // for an IPv4, we would have 4 byte array items, for IPv6, 16 byte array items
        for (byte b : ipBase.getAddress()) {
            ipAsNumeric = ipAsNumeric.shiftLeft(8);
            ipAsNumeric = ipAsNumeric.or(BigInteger.valueOf(b & 0xFF));
        }
        return ipAsNumeric;
    }

    public static BigInteger ipToBigInteger(String ip) throws UnknownHostException {
        BigInteger ipAsNumeric = BigInteger.ZERO;

        // we use a loop, as we might have either an IPv4 or an IPv6 address
        // for an IPv4, we would have 4 byte array items, for IPv6, 16 byte array items
        for (byte b : InetAddress.getByName(ip).getAddress()) {
            ipAsNumeric = ipAsNumeric.shiftLeft(8);
            ipAsNumeric = ipAsNumeric.or(BigInteger.valueOf(b & 0xFF));
        }
        return ipAsNumeric;
    }

    public BigInteger getStart() {
        return start;
    }

    public BigInteger getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IpRange ipRange = (IpRange) o;
        return start.equals(ipRange.start) && end.equals(ipRange.end);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "IpRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
