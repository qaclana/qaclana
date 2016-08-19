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
package org.qaclana.services.jpa.control;

import org.qaclana.api.control.AuditService;
import org.qaclana.api.entity.Audit;
import org.qaclana.api.entity.IpRange;
import org.qaclana.services.jpa.entity.AuditEntity;
import org.qaclana.services.jpa.entity.AuditEntity_;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Juraci Paixão Kröhling
 */
@Stateless
public class AuditServiceJPA implements AuditService {
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    EntityManager entityManager;

    @Override
    public List<Audit> listEventsForRequestId(String requestId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditEntity> query = builder.createQuery(AuditEntity.class);
        Root<AuditEntity> root = query.from(AuditEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(AuditEntity_.requestId), requestId));

        List<AuditEntity> results = entityManager.createQuery(query).getResultList();
        return results.stream().map(AuditEntity::toAudit).collect(Collectors.toList());
    }

    @Override
    public List<Audit> listEventsForClientIp(InetAddress ipAddress) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditEntity> query = builder.createQuery(AuditEntity.class);
        Root<AuditEntity> root = query.from(AuditEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(AuditEntity_.ipAddress), ipAddress));

        List<AuditEntity> results = entityManager.createQuery(query).getResultList();
        return results.stream().map(AuditEntity::toAudit).collect(Collectors.toList());
    }

    @Override
    public List<Audit> listEventsForClientIp(IpRange ipRange) {
        BigInteger start = ipRange.getStart();
        BigInteger end = ipRange.getStart();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditEntity> query = builder.createQuery(AuditEntity.class);
        Root<AuditEntity> root = query.from(AuditEntity.class);
        query.select(root);
        query.where(builder.between(root.get(AuditEntity_.ipAddress), start, end));

        List<AuditEntity> results = entityManager.createQuery(query).getResultList();
        return results.stream().map(AuditEntity::toAudit).collect(Collectors.toList());
    }

    @Override
    public void add(Audit audit) {
        AuditEntity entity = getEntity(audit.getId());
        if (null != entity) {
            return;
        }
        entityManager.persist(new AuditEntity(audit));
    }

    @Override
    public void remove(UUID id) {
        AuditEntity entity = getEntity(id);
        if (null == entity) {
            return;
        }
        entityManager.remove(entity);
    }

    @Override
    public Audit get(UUID id) {
        AuditEntity entity = getEntity(id);
        if (null == entity) {
            return null;
        }
        return entity.toAudit();
    }

    private AuditEntity getEntity(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditEntity> query = builder.createQuery(AuditEntity.class);
        Root<AuditEntity> root = query.from(AuditEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(AuditEntity_.id), id));

        List<AuditEntity> results = entityManager.createQuery(query).getResultList();
        if (results.size() == 1) {
            return results.get(0);
        }

        if (results.size() > 1) {
            throw new IllegalStateException("Duplicate Audit event with ID " + id);
        }

        return null;
    }

}
