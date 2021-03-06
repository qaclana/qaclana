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

package handler

import (
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"

	"gitlab.com/qaclana/qaclana/pkg/proxy/director"
)

// NewProxyHandler creates a handler that filters the incoming request and outgoing responses
func NewProxyHandler(target string) http.Handler {
	rpURL, err := url.Parse(target)
	if err != nil {
		log.Fatal(err)
	}
	p := httputil.NewSingleHostReverseProxy(rpURL)
	p.Transport = director.NewRoundTripper(p.Transport)

	return p
}
