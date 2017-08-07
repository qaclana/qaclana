// Copyright © 2017 The Qaclana Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Package client groups the connections from the Qaclana Server to the external services
// like the backend
package client

import (
	"fmt"
	"log"
	"time"

	"golang.org/x/net/context"
	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

var connected = make(chan struct{})

// Start starts a connection to the backend server
func Start(backendHostname string, backendGrpcPort int) {
	// try to immediately start and connect
	// if this returns, it means it failed, so, we retry
	doStart(backendHostname, backendGrpcPort)
	log.Printf("Retrying to connect to %s:%d", backendHostname, backendGrpcPort)

	// we could use a more sophisticated logic here, but for now,
	// attempting every second is OK
	tick := time.Tick(1 * time.Second)
	for {
		select {
		case <-tick:
			doStart(backendHostname, backendGrpcPort)
		}
	}
}

func doStart(backendHostname string, backendGrpcPort int) {
	log.Printf("Connecting to the backend server at %s:%d", backendHostname, backendGrpcPort)
	conn, err := grpc.Dial(fmt.Sprintf("%s:%d", backendHostname, backendGrpcPort), grpc.WithInsecure())
	if err != nil {
		log.Printf("did not connect: %v", err)
		return
	}
	defer conn.Close()

	c := qaclana.NewSystemStateServiceClient(conn)
	stream, err := c.Receive(context.Background(), &qaclana.Empty{})
	if err != nil {
		log.Printf("could not receive event: %v", err)
		return
	}

	for {
		state, err := stream.Recv()
		if err != nil {
			log.Printf("unexpected error: %v", err)
			return
		}

		log.Printf("received state %s from the server", state.State)
	}
}
