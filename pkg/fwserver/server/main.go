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

// Package server contains utility code for starting new firewall servers
package server

import (
	"log"
	"net"
	"net/http"

	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/fwserver/handler"
)

// StartHTTPServer starts a new HTTP server based on the given bind directions
func StartHTTPServer(bindTo string) *http.Server {
	log.Printf("Starting HTTP interface at %s", bindTo)

	mu := http.NewServeMux()
	mu.HandleFunc("/", handler.HTTPHandler)

	h := &http.Server{Handler: mu}

	// go log.Fatal(http.ListenAndServe(bindTo, mu))

	log.Printf("Started  HTTP interface at %s", bindTo)
	return h
}

// StartGRPCServer starts a new GRPC server based on the given bind directions
func StartGRPCServer(bindTo string) (*grpc.Server, error) {
	log.Printf("Starting gRPC interface at %s", bindTo)

	_, err := net.Listen("tcp", bindTo)
	if err != nil {
		log.Printf("failed to listen at: %v", err)
		return nil, err
	}

	server := grpc.NewServer()
	handler.RegisterGrpcHandler(server)

	// go log.Fatalf("Failed to serve: %v", server.Serve(listener))

	log.Printf("Started gRPC interface at %s", bindTo)
	return server, nil
}
