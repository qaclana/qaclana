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
	"log"
	"net/http"

	_ "gitlab.com/qaclana/qaclana/pkg/processor/whitelist" // just load the processor for self-registration
	"gitlab.com/qaclana/qaclana/pkg/proxy/handler"
)

// Start a new HTTP server bound to the given address
func Start(bindTo string, target string) *http.Server {
	log.Print("Starting Qaclana Proxy")
	s := handler.NewProxyHandler(target)

	mu := http.NewServeMux()
	mu.Handle("/", s)

	h := &http.Server{Handler: mu}

	log.Printf("Started Proxy at %s, protecting %s", bindTo, target)
	go func() {
		log.Printf("unable to serve: %v", http.ListenAndServe(bindTo, mu))
	}()

	return h
}
