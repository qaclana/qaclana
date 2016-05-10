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
package org.qaclana.services.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qaclana.api.control.BlocklistService;
import org.qaclana.api.entity.IpRange;
import org.qaclana.services.jpa.control.BlocklistResources;
import org.qaclana.services.jpa.control.BlocklistServiceJPA;
import org.qaclana.services.jpa.entity.IpRangeEntity;

import javax.inject.Inject;
import java.io.File;

/**
 * @author Juraci Paixão Kröhling
 */
@RunWith(Arquillian.class)
public class BasicPersistenceTest {

    @Inject
    BlocklistService blocklistService;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(BlocklistServiceJPA.class.getPackage())
                .addPackage(BlocklistResources.class.getPackage())
                .addPackage(IpRangeEntity.class.getPackage())
                .addPackage(BlocklistService.class.getPackage())
                .addPackage(IpRange.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                .addAsLibraries(Maven.resolver().resolve("org.mockito:mockito-all:1.10.19").withoutTransitivity().as(File.class));
    }

    @Test
    public void simplePersistenceTestForIpRange() {
        blocklistService.add(IpRange.fromString("192.168.1.0/24"));
    }
}