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
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"

	"github.com/spf13/viper"
	"google.golang.org/grpc"

	"gitlab.com/qaclana/qaclana/pkg/backend/handler"
	"gitlab.com/qaclana/qaclana/pkg/backend/sysstate"
	hcServer "gitlab.com/qaclana/qaclana/pkg/healthcheck/server"
)

// Start initializes all the required handlers for the backend server.
func Start() {
	log.Print("Starting Qaclana Backend")
	hcServer.Start(viper.GetInt("healthcheck-port"))
	sysstate.StartBroadcaster()

	var serverChannel = make(chan os.Signal, 0)
	signal.Notify(serverChannel, os.Interrupt, syscall.SIGTERM)

	go func() {
		// TODO: this looks bad at first, but this is the simplest thing that works, once we know what else we need, we'll change this
		dbURL := viper.GetString("database-url")
		sysstate.DatabaseUrl = dbURL

		mainServerMux := http.NewServeMux()
		mainServerMux.HandleFunc("/", handler.RootHttpHandler)
		mainServerMux.HandleFunc("/v1/system-state", handler.SystemStateHandler)
		port := viper.GetInt("port")
		address := fmt.Sprintf("0.0.0.0:%d", port)
		log.Printf("Started Backend at %s", address)
		log.Fatal(http.ListenAndServe(address, mainServerMux))
	}()
	go func() {
		port := viper.GetInt("grpc-port")
		address := fmt.Sprintf("0.0.0.0:%d", port)
		log.Printf("Started gRPC interface at %s", address)

		listener, err := net.Listen("tcp", address)
		if err != nil {
			log.Fatalf("Failed to listen at: %v", err)
		}
		server := grpc.NewServer()
		handler.RegisterGrpcHandler(server)

		if err := server.Serve(listener); err != nil {
			log.Fatalf("Failed to serve: %v", err)
		}
	}()

	select {
	case <-serverChannel:
		log.Println("Qaclana Backend is finishing")
	}
}
