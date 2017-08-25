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

// Package server groups the resources related to the server handlers of the backend component.
package server

import (
	"log"
	"net"
	"net/http"

	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/backend/handler"
	"gitlab.com/qaclana/qaclana/pkg/sysstate"
)

// StartHTTPServer starts a new HTTP server with all the backend handlers
func StartHTTPServer(bindTo string, storage sysstate.Storage) *http.Server {
	log.Print("Starting Qaclana Backend")
	s := handler.NewSysStateHandler(storage)

	mu := http.NewServeMux()
	mu.Handle("/v1/system-state", s)

	h := &http.Server{Handler: mu}

	log.Printf("Started Backend at %s", bindTo)
	go func() {
		log.Printf("failed to serve: %v", http.ListenAndServe(bindTo, mu))
	}()

	return h
}

// StartGrpcServer starts a new GRPC server with all backend handlers
func StartGrpcServer(bindTo string, storage sysstate.Storage) (*grpc.Server, error) {
	log.Printf("Starting gRPC interface at %s", bindTo)

	listener, err := net.Listen("tcp", bindTo)
	if err != nil {
		log.Printf("failed to listen at: %v", err)
		return nil, err
	}

	server := grpc.NewServer()
	handler.RegisterGrpcHandler(server, storage)
	go func() {
		log.Printf("Failed to serve: %v", server.Serve(listener))
	}()

	return server, nil
}
