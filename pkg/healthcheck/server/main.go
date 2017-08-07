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

	"gitlab.com/qaclana/qaclana/pkg/healthcheck/handler"
)

func Start(port int) {
	go func() {
		healthCheckMux := http.NewServeMux()
		healthCheckMux.HandleFunc("/", handler.HealthCheckHandler)
		address := fmt.Sprintf("0.0.0.0:%d", port)
		log.Printf("Started Health Check Server at %s", address)
		log.Fatal(http.ListenAndServe(address, healthCheckMux))
	}()
}
