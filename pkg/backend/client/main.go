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
	"log"
	"time"

	"golang.org/x/net/context"
	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

// Client represents a client connection to the backend
type Client struct {
	OnConnect func()
	conn      *grpc.ClientConn
	backend   string
}

// NewClient constructs a new client to connect to the backend
func NewClient(backend string) *Client {
	return &Client{backend: backend, OnConnect: func() {}}
}

// Start a new connection on a new client
func (c *Client) Start() {
	c.connect()
	go c.doStart()
}

// Close the underlying connection for this client
func (c *Client) Close() {
	if c.conn != nil {
		c.conn.Close()
	}
}

func (c *Client) connect() {
	log.Printf("Connecting to the backend server at %s", c.backend)
	conn, err := grpc.Dial(c.backend, grpc.WithInsecure())
	if err != nil {
		log.Printf("did not connect: %v", err)
		return
	}

	c.conn = conn
}

func (c *Client) doStart() {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	client := qaclana.NewSystemStateServiceClient(c.conn)
	stream, err := client.Receive(ctx, &qaclana.Empty{})
	if err != nil {
		log.Printf("could not receive event: %v", err)
		// we could use a more sophisticated logic here, but for now,
		// attempting every second is OK
		c.retry()
		return
	}

	c.OnConnect()

	for {
		_, err := stream.Recv()
		if err != nil {
			log.Printf("unexpected error: %v", err)
			c.retry()
			return
		}
	}
}

func (c *Client) retry() {
	// we could use a more sophisticated logic here, but for now,
	// attempting every second is OK
	<-time.Tick(time.Second)
	c.doStart()
}
