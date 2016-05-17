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
package org.qaclana.backend.boundary;

import org.qaclana.api.control.WhitelistService;
import org.qaclana.api.entity.IpRange;
import org.qaclana.backend.entity.rest.IpRangeRequest;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * JAX-RS endpoint exposing management operations for the {@link IpRange} whitelist.
 * The methods just perform some data validation, delegate the operation to the {@link WhitelistService} and wrap
 * the response into a {@link Response}.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/whitelist")
@Stateless
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WhitelistEndpoint {
    @Inject
    WhitelistService whitelistService;

    @GET
    public Response getAllBlockedIpRanges() {
        List<IpRange> allIpRanges = whitelistService.list();
        return Response.ok(allIpRanges).build();
    }

    @DELETE
    @Path("{ipRange}")
    public Response deleteRange(@PathParam("ipRange") String ipRange) {
        whitelistService.remove(IpRange.fromString(ipRange));
        return Response.noContent().build();
    }

    @POST
    public Response addRange(IpRangeRequest ipRangeRequest) {
        IpRange range = IpRange.fromString(ipRangeRequest.getIpRange());
        whitelistService.add(range);
        return Response.ok(range).build();
    }
}
