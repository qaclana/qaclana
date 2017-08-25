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

package client

import (
	"fmt"
	"log"
	"net"
	"sync"
	"testing"

	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/backend/handler"
	"gitlab.com/qaclana/qaclana/pkg/proto"
	"gitlab.com/qaclana/qaclana/pkg/sysstate/inmemory"
)

func TestClientCanConnect(t *testing.T) {
	listener, err := net.Listen("tcp", "localhost:0")
	if err != nil {
		t.Fatalf("failed to listen at: %v", err)
	}
	p := listener.Addr().(*net.TCPAddr).Port

	wg := sync.WaitGroup{}
	startServer(t, listener)
	c := NewClient(fmt.Sprintf("localhost:%d", p))
	defer c.Close()

	wg.Add(1)
	c.OnConnect = func() {
		wg.Done()
		log.Println("Connected to the backend.")
	}

	c.Start()
	wg.Wait()
}
func TestClientCanReconnect(t *testing.T) {
	listener, err := net.Listen("tcp", "localhost:0")
	if err != nil {
		t.Fatalf("failed to listen at: %v", err)
	}
	p := listener.Addr().(*net.TCPAddr).Port

	wg := sync.WaitGroup{}
	c := NewClient(fmt.Sprintf("localhost:%d", p))
	defer c.Close()

	wg.Add(1)
	c.OnConnect = func() {
		wg.Done()
		log.Println("Connected to the backend.")
	}

	c.Start()
	wg.Wait()
}

func startServer(t *testing.T, listener net.Listener) {
	server := grpc.NewServer()
	handler.RegisterGrpcHandler(server, inmemory.WithState(qaclana.State_DISABLED))
	go func() {
		if err := server.Serve(listener); err != nil {
			t.Fatalf("failed to serve: %v", err)
		}
	}()
}
