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
package org.qaclana.addons.blocklistupdater;

import org.qaclana.api.control.BlocklistService;
import org.qaclana.api.entity.IpRange;

import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Juraci Paixão Kröhling
 */
@Singleton
public class BlocklistServiceTestImpl implements BlocklistService {
    List<IpRange> ipRangeList = new ArrayList<>();

    @Override
    public void add(IpRange ipRange) {
        ipRangeList.add(ipRange);
    }

    @Override
    public void remove(IpRange ipRange) {
        ipRangeList.remove(ipRange);
    }

    @Override
    public boolean isInBlockList(IpRange ipRange) {
        return ipRangeList.contains(ipRange);
    }

    List<IpRange> getIpRangeList() {
        return Collections.unmodifiableList(ipRangeList);
    }
}
