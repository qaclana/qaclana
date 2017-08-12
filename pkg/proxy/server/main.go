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
	"net/http"
	"os"
	"os/signal"
	"syscall"

	"github.com/spf13/viper"

	"gitlab.com/qaclana/qaclana/pkg/fwserver/client"
	hcServer "gitlab.com/qaclana/qaclana/pkg/healthcheck/server"
)

func Start() {
	log.Print("Starting Qaclana Proxy")
	hcServer.Start(viper.GetInt("healthcheck-port"))

	var serverChannel = make(chan os.Signal, 0)
	signal.Notify(serverChannel, os.Interrupt, syscall.SIGTERM)

	go func() {
		mainServerMux := http.NewServeMux()
		mainServerMux.HandleFunc("/", handler)
		port := viper.GetInt("port")
		address := fmt.Sprintf("0.0.0.0:%d", port)
		log.Printf("Started Proxy at %s", address)
		log.Fatal(http.ListenAndServe(address, mainServerMux))
	}()
	go func() {
		serverHostname := viper.GetString("server-hostname")
		client.Start(serverHostname, 10000)
	}()

	select {
	case <-serverChannel:
		log.Println("Qaclana Proxy is finishing")
	}
}

func handler(w http.ResponseWriter, _ *http.Request) {
	fmt.Fprintln(w, "OK")
}

func healthCheckHandler(w http.ResponseWriter, _ *http.Request) {
	fmt.Fprintln(w, "OK")
}
