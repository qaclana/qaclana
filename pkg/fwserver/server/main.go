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

	"gitlab.com/qaclana/qaclana-server/pkg/client"
	"gitlab.com/qaclana/qaclana-server/pkg/handler"
	hcServer "gitlab.com/qaclana/qaclana/pkg/healthcheck/server"
)

func Start() {
	log.Print("Starting Qaclana Server")
	hcServer.Start(viper.GetInt("healthcheck-port"))

	var serverChannel = make(chan os.Signal, 0)
	signal.Notify(serverChannel, os.Interrupt, syscall.SIGTERM)

	go func() {
		mainServerMux := http.NewServeMux()
		mainServerMux.HandleFunc("/", handler.HttpHandler)
		port := viper.GetInt("port")
		address := fmt.Sprintf("0.0.0.0:%d", port)
		log.Printf("Started Server at %s", address)
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
	go func() {
		client.Start(viper.GetString("backend-hostname"), viper.GetInt("backend-grpc-port"))
	}()

	select {
	case <-serverChannel:
		log.Println("Qaclana Server is finishing")
	}
}
