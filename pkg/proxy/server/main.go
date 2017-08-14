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

// Package server has all components required to build and start a proxy server
package server

import (
	"fmt"
	"log"
	"net/http"
)

// Start a new HTTP server bound to the given address
func Start(bindTo string) *http.Server {
	log.Print("Starting Qaclana Proxy")

	mu := http.NewServeMux()
	mu.HandleFunc("/", handler)

	h := &http.Server{Handler: mu}

	log.Printf("Started Proxy at %s", bindTo)
	go func() {
		log.Printf("unable to serve: %v", http.ListenAndServe(bindTo, mu))
	}()

	return h
}

func handler(w http.ResponseWriter, _ *http.Request) {
	fmt.Fprintln(w, "OK")
}
