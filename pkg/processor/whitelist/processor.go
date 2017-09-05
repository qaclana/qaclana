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

// Package whitelist specifies a processor that matches the request with a whitelist
package whitelist

import (
	"fmt"
	"log"
	"net"
	"net/http"

	opentracing "github.com/opentracing/opentracing-go"

	"gitlab.com/qaclana/qaclana/pkg/processor"
)

var p = &pr{}

// pr is the whitelist's processor implementation
type pr struct {
	rs []*net.IPNet
}

// Process an incoming request, matching its remote address with the white list
func (p *pr) Process(req *http.Request) (processor.Outcome, error) {
	sp, _ := opentracing.StartSpanFromContext(req.Context(), "processor-whitelist")
	defer sp.Finish()

	ip, _, err := net.SplitHostPort(req.RemoteAddr)
	if err != nil {
		log.Printf("whitelist: %s", err)
		// let's try to parse it as if it's a IP-only string:
		ip = req.RemoteAddr
	}

	client := net.ParseIP(ip)
	if client == nil {
		return processor.NEUTRAL, fmt.Errorf("whitelist: %s could not be parsed into an IP", ip)
	}

	for _, r := range p.rs {
		if r.Contains(client) {
			return processor.ALLOW, nil
		}
	}
	return processor.NEUTRAL, nil
}

func (p *pr) Add(r *net.IPNet) {
	p.rs = append(p.rs, r)
}

func (p *pr) String() string {
	return "Whitelist"
}
