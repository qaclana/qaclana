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

	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"

	"gitlab.com/qaclana/qaclana/pkg/proto"
	"gitlab.com/qaclana/qaclana/pkg/sysstate"
)

// QaclanaGrpcBackend is a representation of the operations available remotely
type QaclanaGrpcBackend struct {
	storage sysstate.Storage
}

// Receive updates to the system state, retrieving first the current state and blocking until further updates
// are available
func (s *QaclanaGrpcBackend) Receive(in *qaclana.Empty, stream qaclana.SystemStateService_ReceiveServer) error {
	log.Printf("New client registered.")
	defer func() {
		log.Printf("Client went out.")
	}()

	// sending the current state:
	current, _ := s.storage.Current()
	stream.Send(&qaclana.SystemState{State: current})

	c, _ := s.storage.Notifier()
	for {
		select {
		case state := <-c:
			stream.Send(&qaclana.SystemState{State: state})
			log.Println("A system state event has been sent downstream: ", state)
		}
	}
}

// RegisterGrpcHandler self registers this handler with the GRPC server
func RegisterGrpcHandler(s *grpc.Server, storage sysstate.Storage) {
	qaclana.RegisterSystemStateServiceServer(s, &QaclanaGrpcBackend{storage: storage})
	reflection.Register(s)
}
