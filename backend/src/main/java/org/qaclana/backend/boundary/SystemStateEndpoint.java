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

import org.qaclana.backend.control.MsgLogger;
import org.qaclana.backend.control.SystemStateContainer;
import org.qaclana.backend.entity.SystemState;
import org.qaclana.backend.entity.event.SystemStateChange;
import org.qaclana.backend.entity.rest.ErrorResponse;
import org.qaclana.backend.entity.rest.SystemStateRequest;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * REST endpoint for changing the system state.
 *
 * @author Juraci Paixão Kröhling
 */
@Path("/system-state")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Stateless
public class SystemStateEndpoint {
    private static final MsgLogger log = MsgLogger.LOGGER;

    @Inject
    Event<SystemStateChange> systemStateChangeEvent;

    @Inject
    SystemStateContainer systemStateInstance;

    @PUT
    public Response update(SystemStateRequest request) {
        log.systemStateChangeRequestReceived(request.getState());
        SystemState state = null;
        for (SystemState value : SystemState.values()) {
            if (value.name().equalsIgnoreCase(request.getState())) {
                state = value;
            }
        }

        if (null == state) {
            ErrorResponse response = new ErrorResponse(
                    "invalid_system_state",
                    "System state is invalid. Possible values: " + Arrays.toString(SystemState.values())
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        systemStateInstance.setState(state);
        systemStateChangeEvent.fire(new SystemStateChange(state));
        return Response.ok().entity(state).build();
    }

    @GET
    public Response get() {
        return Response.ok().entity(systemStateInstance).build();
    }
}
