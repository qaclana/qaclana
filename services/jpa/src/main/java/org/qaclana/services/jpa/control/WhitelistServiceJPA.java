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
package org.qaclana.services.jpa.control;

import org.qaclana.api.control.WhitelistService;
import org.qaclana.api.entity.IpRange;
import org.qaclana.services.jpa.entity.IpRangeEntity;
import org.qaclana.services.jpa.entity.IpRangeEntity_;
import org.qaclana.services.jpa.entity.IpRangeType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class WhitelistServiceJPA implements WhitelistService {
    @Inject
    EntityManager entityManager;

    @Override
    public List<IpRange> list() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<IpRangeEntity> query = builder.createQuery(IpRangeEntity.class);
        Root<IpRangeEntity> root = query.from(IpRangeEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(IpRangeEntity_.ipRangeType), IpRangeType.WHITELIST));

        List<IpRangeEntity> results = entityManager.createQuery(query).getResultList();
        return results.stream().map(IpRangeEntity::toIpRange).collect(Collectors.toList());
    }

    @Override
    public void add(IpRange ipRange) {
        if (!isInWhitelist(ipRange)) {
            entityManager.persist(new IpRangeEntity(ipRange, IpRangeType.WHITELIST));
        }
    }

    @Override
    public void remove(IpRange ipRange) {
        if (isInWhitelist(ipRange)) {
            entityManager.remove(get(ipRange));
        }
    }

    @Override
    public boolean isInWhitelist(IpRange ipRange) {
        return null != get(ipRange);
    }

    private IpRangeEntity get(IpRange ipRange) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<IpRangeEntity> query = builder.createQuery(IpRangeEntity.class);
        Root<IpRangeEntity> root = query.from(IpRangeEntity.class);
        query.select(root);
        query.where(
                builder.equal(root.get(IpRangeEntity_.start), ipRange.getStart()),
                builder.equal(root.get(IpRangeEntity_.end), ipRange.getEnd()),
                builder.equal(root.get(IpRangeEntity_.ipRangeType), IpRangeType.WHITELIST)
        );

        List<IpRangeEntity> results = entityManager.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("Duplicate IP range found for " + ipRange);
        }

        return null;
    }
}
