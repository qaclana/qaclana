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

// Package client provides facilities to creating a new firewall client
package client

import (
	"fmt"
	"log"

	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

var (
	systemState    qaclana.State
	serverHostname string
)

// Start a new client connection with the firewall server
func Start(serverHostname string, grpcPort int) {
	log.Printf("Connecting to the server at %s:%d", serverHostname, grpcPort)
	conn, err := grpc.Dial(fmt.Sprintf("%s:%d", serverHostname, grpcPort), grpc.WithInsecure())
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
}
