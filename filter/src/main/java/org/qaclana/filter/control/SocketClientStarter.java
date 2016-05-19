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
package org.qaclana.filter.control;

import org.qaclana.filter.entity.ConnectToSocketServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Juraci Paixão Kröhling
 */
@Startup
@Singleton
@DependsOn("SocketClient") // otherwise, the socket client might get undeployed before this
public class SocketClientStarter {
    private static final MsgLogger log = MsgLogger.LOGGER;
    private static final int MAX_WAIT = 20_000; // in milliseconds
    static final CloseReason UNDEPLOYMENT = new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Client being undeployed.");
    private ScheduledFuture scheduledFuture;

    @Resource
    private ManagedScheduledExecutorService executor;

    @Inject
    @SocketServerEndpointUri
    URI uri;

    @Inject
    SocketSessionContainer sessionContainer;

    @Inject
    SocketClient socketClient;

    @Inject
    Event<ConnectToSocketServer> connectToSocketServerEvent;

    @PostConstruct
    public void connect() {
        connectToSocketServerEvent.fire(new ConnectToSocketServer(null, System.currentTimeMillis()));
    }

    @PreDestroy
    public void close() {
        try {
            if (null != scheduledFuture && !scheduledFuture.isDone()) {
                scheduledFuture.cancel(true);
            }

            if (null != sessionContainer.getSession()) {
                sessionContainer.getSession().close(UNDEPLOYMENT);
            }
        } catch (Throwable t) {
            log.undeployingCloseFailed(t);
        }
    }

    @Asynchronous
    public void reconnect(@Observes ConnectToSocketServer connectToSocketServer) {
        // there's a chance that we might receive two such events but store only one in our instance
        // too bad... the solution would be to have a local cache of all the futures we called and clean them
        // up from time to time... which sounds overkill for this edge case
        scheduledFuture = executor.schedule(() -> {
            try {
                log.reconnectingWebSocket();
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                sessionContainer.setSession(container.connectToServer(socketClient, uri));
            } catch (DeploymentException | IOException e) {
                log.cannotOpenSocketToServer(e);

                long nextAttemptIn = Math.min(MAX_WAIT, connectToSocketServer.getAttempt() * 1000);
                long nextAttemptAt = System.currentTimeMillis() + nextAttemptIn;
                connectToSocketServer.setNextAttempt(nextAttemptAt);
                connectToSocketServer.increaseAttempt();
                log.failedToReconnect(Instant.ofEpochMilli(nextAttemptAt).toString());

                connectToSocketServerEvent.fire(connectToSocketServer);
            }
        }, Math.min(MAX_WAIT, connectToSocketServer.getAttempt() * 1000), TimeUnit.MILLISECONDS);
    }
}
