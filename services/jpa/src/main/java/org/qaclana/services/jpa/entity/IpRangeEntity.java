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
package org.qaclana.services.jpa.entity;

import org.qaclana.api.entity.IpRange;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import java.math.BigInteger;

/**
 * @author Juraci Paixão Kröhling
 */
@Entity
public class IpRangeEntity extends QaclanaEntity {
    private BigInteger start;
    private BigInteger end;

    @Enumerated
    private IpRangeType ipRangeType;

    protected IpRangeEntity() {
    }

    public IpRangeEntity(IpRange ipRange, IpRangeType ipRangeType) {
        this.start = ipRange.getStart();
        this.end = ipRange.getEnd();
        this.ipRangeType = ipRangeType;
    }

    public BigInteger getStart() {
        return start;
    }

    public BigInteger getEnd() {
        return end;
    }

    public IpRangeType getIpRangeType() {
        return ipRangeType;
    }

    @Override
    public String toString() {
        return "IpRangeEntity{" +
                "id=" + getId() +
                ", start=" + start +
                ", end=" + end +
                ", type=" + ipRangeType +
                "} ";
    }

    public IpRange toIpRange() {
        return new IpRange(this.getStart(), this.getEnd());
    }
}
