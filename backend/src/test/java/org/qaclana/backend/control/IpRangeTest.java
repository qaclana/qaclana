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
package org.qaclana.backend.control;

import org.junit.Test;
import org.qaclana.api.entity.IpRange;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * @author Juraci Paixão Kröhling
 */
public class IpRangeTest {

    @Test
    public void testValidRanges() throws UnknownHostException {
        BigInteger ip = IpRange.ipToBigInteger(InetAddress.getByName("2001:0db8:0000:0042:0000:8a2e:0370:7334"));
        IpRange ipRange = IpRange.fromString("2001:0db8:0000:0042:0000:8a2e:0370:7334");
        assertEquals(ip, ipRange.getStart());
        assertEquals(ip, ipRange.getEnd());

        ip = IpRange.ipToBigInteger("2001:0db8:0000:0042:0000:8a2e:0370:7334");
        ipRange = IpRange.fromString("2001:0db8:0000:0042:0000:8a2e:0370:7334/128");
        assertEquals(ip, ipRange.getStart());
        assertEquals(ip, ipRange.getEnd());

        ip = IpRange.ipToBigInteger("127.0.0.1");
        ipRange = IpRange.fromString("127.0.0.1");
        assertEquals(ip, ipRange.getStart());
        assertEquals(ip, ipRange.getEnd());

        ip = IpRange.ipToBigInteger("127.0.0.1");
        ipRange = IpRange.fromString("127.0.0.1/32");
        assertEquals(ip, ipRange.getStart());
        assertEquals(ip, ipRange.getEnd());

        ipRange = IpRange.fromString("192.168.0.1/24");
        assertEquals(IpRange.ipToBigInteger("192.168.0.0"), ipRange.getStart());
        assertEquals(IpRange.ipToBigInteger("192.168.0.255"), ipRange.getEnd());

        ipRange = IpRange.fromString("192.168.0.30/24");
        assertEquals(IpRange.ipToBigInteger("192.168.0.0"), ipRange.getStart());
        assertEquals(IpRange.ipToBigInteger("192.168.0.255"), ipRange.getEnd());

        ipRange = IpRange.fromString("fe80::/64");
        assertEquals(IpRange.ipToBigInteger("fe80:0000:0000:0000:0000:0000:0000:0000"), ipRange.getStart());
        assertEquals(IpRange.ipToBigInteger("fe80:0000:0000:0000:ffff:ffff:ffff:ffff"), ipRange.getEnd());

        ipRange = IpRange.fromString("fe80::/128");
        assertEquals(IpRange.ipToBigInteger("fe80:0000:0000:0000:0000:0000:0000:0000"), ipRange.getStart());
        assertEquals(IpRange.ipToBigInteger("fe80:0000:0000:0000:0000:0000:0000:0000"), ipRange.getEnd());
    }

    @Test
    public void testConvertIpToBigNumber() throws UnknownHostException {
        assertEquals(
                new BigInteger("42540766452641195744311209248773141316"),
                IpRange.ipToBigInteger("2001:0db8:85a3:08d3:1319:8a2e:0370:7344")
        );
        assertEquals(
                new BigInteger("2130706433"),
                IpRange.ipToBigInteger("127.0.0.1")
        );
        assertEquals(
                new BigInteger("3232235521"),
                IpRange.ipToBigInteger("::192.168.0.1")
        );
        assertEquals(
                new BigInteger("3232235521"),
                IpRange.ipToBigInteger("::ffff:192.168.0.1")
        );
        assertEquals(
                new BigInteger("3232235521"),
                IpRange.ipToBigInteger("192.168.0.1")
        );
        assertEquals(
                new BigInteger("42540766411282592856903984951653826561"),
                IpRange.ipToBigInteger("2001:db8::1")
        );
        assertEquals(
                new BigInteger("1"),
                IpRange.ipToBigInteger("::1")
        );
        assertEquals(
                new BigInteger("50676817340170353689313142918267011088"),
                IpRange.ipToBigInteger("2620:0:2d0:200::10")
        );
        assertEquals(
                new BigInteger("338288524927261089654018896841347694592"),
                IpRange.ipToBigInteger("FE80::")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void endIpIsBiggerThanStartIp() throws UnknownHostException {
        new IpRange(IpRange.ipToBigInteger("127.0.0.2"), IpRange.ipToBigInteger("127.0.0.1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void routingPrefixBiggerThanMaximumOnIPv4() throws UnknownHostException {
        IpRange.fromString("127.0.0.1/64");
    }

    @Test(expected = IllegalArgumentException.class)
    public void routingPrefixBiggerThanMaximumOnIPv6() throws UnknownHostException {
        IpRange.fromString("::1/129");
    }
}
