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
package handler

import (
	"fmt"
	"net"
	"testing"

	"golang.org/x/net/context"
	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

func TestQaclanaGrpcServer_Process(t *testing.T) {
	port := startServer(t)
	conn, err := grpc.Dial(fmt.Sprintf("0.0.0.0:%d", port), grpc.WithInsecure())
	if err != nil {
		t.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	c := qaclana.NewRequestServiceClient(conn)
	t.Run("Process empty request", func(t *testing.T) {
		_, err := c.Process(context.Background(), &qaclana.Request{})
		if err != nil {
			t.Fatalf("could not greet: %v", err)
		}
	})

}

func startServer(t *testing.T) int {
	listener, err := net.Listen("tcp", "localhost:0")
	if err != nil {
		t.Fatalf("Failed to listen at: %v", err)
	}
	server := grpc.NewServer()
	RegisterGrpcHandler(server)
	go func() {
		if err := server.Serve(listener); err != nil {
			t.Fatalf("Failed to serve: %v", err)
		}
	}()
	return listener.Addr().(*net.TCPAddr).Port
}
