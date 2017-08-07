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
package handler_test

import (
	"fmt"
	"net"
	"testing"

	"golang.org/x/net/context"
	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/backend/handler"
	"gitlab.com/qaclana/qaclana/pkg/backend/sysstate"
	"gitlab.com/qaclana/qaclana/pkg/proto"
)

func TestQaclanaGrpcBackend_ReceiveOnConnect(t *testing.T) {
	port := startServer(t)
	conn, err := grpc.Dial(fmt.Sprintf("0.0.0.0:%d", port), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	c := qaclana.NewSystemStateServiceClient(conn)
	t.Run("Process system state received after the connection", func(t *testing.T) {
		// first test: check that we receive the current status when the connection is established
		stream, err := c.Receive(context.Background(), &qaclana.Empty{})
		if err != nil {
			t.Fatalf("could not receive event: %v", err)
		}
		state, _ := stream.Recv()
		if state.State != qaclana.State_DISABLED {
			t.Fatalf("state should have been DISABLED, but is %s", state.State)
		}
	})
}

func TestQaclanaGrpcBackend_ReceiveOnChange(t *testing.T) {
	sysstate.StartBroadcaster()
	port := startServer(t)
	conn, err := grpc.Dial(fmt.Sprintf("0.0.0.0:%d", port), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	c := qaclana.NewSystemStateServiceClient(conn)
	t.Run("Process system state event changes", func(t *testing.T) {
		// first test: check that we receive the current status when the connection is established
		stream, err := c.Receive(context.Background(), &qaclana.Empty{})
		if err != nil {
			t.Fatalf("could not receive event: %v", err)
		}

		// ignore the first event
		stream.Recv()

		// second test: check that we receive updates whenever the system state has changed
		sysstate.Set(qaclana.State_ENFORCING)
		state, _ := stream.Recv()
		if state.State != qaclana.State_ENFORCING {
			t.Fatalf("state should have been ENFORCING, but is %s", state.State)
		}
	})
}

func startServer(t *testing.T) int {
	listener, err := net.Listen("tcp", "localhost:0")
	if err != nil {
		t.Fatalf("failed to listen at: %v", err)
	}
	server := grpc.NewServer()
	handler.RegisterGrpcHandler(server)
	go func() {
		if err := server.Serve(listener); err != nil {
			t.Fatalf("failed to serve: %v", err)
		}
	}()
	return listener.Addr().(*net.TCPAddr).Port
}
