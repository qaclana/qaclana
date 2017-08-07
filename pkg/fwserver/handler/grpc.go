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

// Package handler has a set of handlers for different purposes, like gRPC and regular HTTP
package handler

import (
	"log"

	"golang.org/x/net/context"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"gitlab.com/qaclana/qaclana/pkg/proto"
)

// QaclanaGrpcServer is a representation of the operations available remotely
type QaclanaGrpcServer struct{}

// Process takes incoming HTTP requests and passes through the processors
func (s *QaclanaGrpcServer) Process(ctx context.Context, in *qaclana.Request) (*qaclana.Empty, error) {
	log.Print("Process: Not implemented.")
	return &qaclana.Empty{}, nil
}

// SystemStateChange streams the system state changes to downstream firewall instances
func (s *QaclanaGrpcServer) SystemStateChange(in *qaclana.Empty, stream qaclana.RequestService_SystemStateChangeServer) error {
	log.Print("SystemStateChange: Not implemented.")
	return nil
}

// RegisterGrpcHandler self registers this handler with the GRPC server
func RegisterGrpcHandler(s *grpc.Server) {
	qaclana.RegisterRequestServiceServer(s, &QaclanaGrpcServer{})
	reflection.Register(s)
}
