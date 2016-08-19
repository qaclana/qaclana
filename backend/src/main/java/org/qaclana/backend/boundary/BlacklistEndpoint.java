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
package org.qaclana.backend.boundary;

import org.qaclana.addons.blacklistupdater.MsgLogger;
import org.qaclana.api.control.BlacklistService;
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
 * JAX-RS endpoint exposing management operations for the {@link IpRange} blacklist.
 * The methods just perform some data validation, delegate the operation to the {@link BlacklistService} and wrap
 * the response into a {@link Response}.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/blacklist")
@Stateless
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BlacklistEndpoint {
    private static final MsgLogger logger = MsgLogger.LOGGER;

    @Inject
    BlacklistService blacklistService;

    @GET
    public Response getAllIpRangesInBlacklist() {
        List<IpRange> allIpRanges = blacklistService.list();
        return Response.ok(allIpRanges).build();
    }

    @DELETE
    @Path("{ipRange}")
    public Response deleteRange(@PathParam("ipRange") String ipRange) {
        logger.removeIpRangeFromBlacklist(ipRange);
        blacklistService.remove(IpRange.fromString(ipRange));
        return Response.noContent().build();
    }

    @POST
    public Response addRange(IpRangeRequest ipRangeRequest) {
        logger.addIpRangeToBlacklist(ipRangeRequest.getIpRange());
        IpRange range = IpRange.fromString(ipRangeRequest.getIpRange());
        blacklistService.add(range);
        return Response.ok(range).build();
    }
}
